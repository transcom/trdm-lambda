package com.milmove.trdmlambda.milmove.service;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.RdsUtilities;
import software.amazon.awssdk.services.rds.model.GenerateAuthenticationTokenRequest;

import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.milmove.trdmlambda.milmove.model.LineOfAccounting;
import com.milmove.trdmlambda.milmove.model.TransportationAccountingCode;
import com.milmove.trdmlambda.milmove.util.SecretFetcher;

// README: https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/java-rds.html
@Service
public class DatabaseService {

    private String hostname;
    private Integer port;
    private String dbName;
    private String username;
    private RdsClient rdsClient;

    public DatabaseService(SecretFetcher secretFetcher) {
        this.hostname = secretFetcher.getSecret("rds_hostname");
        this.port = Integer.parseInt(secretFetcher.getSecret("rds_port"));
        this.dbName = secretFetcher.getSecret("rds_db_name");
        this.username = secretFetcher.getSecret("rds_username");
        rdsClient = RdsClient.builder()
                .region(Region.of("us-gov-west-1"))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public Connection getConnection() throws SQLException {

        RdsUtilities utilities = rdsClient.utilities();
        GenerateAuthenticationTokenRequest tokenRequest = GenerateAuthenticationTokenRequest.builder()
                .hostname(hostname)
                .port(port)
                .username(username)
                .region(Region.of("us-gov-west-1"))
                .build();

        String authToken = utilities.generateAuthenticationToken(tokenRequest);
        String jdbcUrl = "jdbc:postgresql://"
                + hostname
                + ":" + port
                + "/" + dbName;

        return DriverManager.getConnection(jdbcUrl, username, authToken);
    }

    // Batch insert 10k TACs at a time
    public void insertTransportationAccountingCodes(List<TransportationAccountingCode> codes) throws SQLException {

        String sql = "INSERT INTO transportation_accounting_codes (id, tac, tac_sys_id, loa_sys_id, tac_fy_txt, tac_fn_bl_mod_cd, org_grp_dfas_cd, tac_mvt_dsg_id, tac_ty_cd, tac_use_cd, tac_maj_clmt_id, tac_bill_act_txt, tac_cost_ctr_nm, buic, tac_hist_cd, tac_stat_cd, trnsprtn_acnt_tx, trnsprtn_acnt_bgn_dt, trnsprtn_acnt_end_dt, dd_actvty_adrs_id, tac_blld_add_frst_ln_tx, tac_blld_add_scnd_ln_tx, tac_blld_add_thrd_ln_tx, tac_blld_add_frth_ln_tx, tac_fnct_poc_nm, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int count = 0;

            for (TransportationAccountingCode code : codes) {
                if (code.getId() == null) {
                    code.setId(UUID.randomUUID());
                }

                pstmt.setObject(1, code.getId());
                pstmt.setString(2, code.getTac());
                pstmt.setString(3, code.getTacSysID());
                pstmt.setString(4, code.getLoaSysID());
                pstmt.setString(5, code.getTacFyTxt());
                pstmt.setString(6, code.getTacFnBlModCd());
                pstmt.setString(7, code.getOrgGrpDfasCd());
                pstmt.setString(8, code.getTacMvtDsgID());
                pstmt.setString(9, code.getTacTyCd());
                pstmt.setString(10, code.getTacUseCd());
                pstmt.setString(11, code.getTacMajClmtID());
                pstmt.setString(12, code.getTacBillActTxt());
                pstmt.setString(13, code.getTacCostCtrNm());
                pstmt.setString(14, code.getBuic());
                pstmt.setString(15, code.getTacHistCd());
                pstmt.setString(16, code.getTacStatCd());
                pstmt.setString(17, code.getTrnsprtnAcntTx());
                pstmt.setDate(18, java.sql.Date.valueOf(code.getTrnsprtnAcntBgnDt().toLocalDate()));
                pstmt.setDate(19, java.sql.Date.valueOf(code.getTrnsprtnAcntEndDt().toLocalDate()));
                pstmt.setString(20, code.getDdActvtyAdrsID());
                pstmt.setString(21, code.getTacBlldAddFrstLnTx());
                pstmt.setString(22, code.getTacBlldAddScndLnTx());
                pstmt.setString(23, code.getTacBlldAddThrdLnTx());
                pstmt.setString(24, code.getTacBlldAddFrthLnTx());
                pstmt.setString(25, code.getTacFnctPocNm());
                pstmt.setTimestamp(26, java.sql.Timestamp.valueOf(code.getUpdatedAt()));
                pstmt.addBatch();

                // Execute every 10000 rows or when finished with the provided TACs
                if (count++ % 10000 == 0 || count == codes.size()) {
                    pstmt.executeBatch();
                }
            }
        }
    }

    // Batch insert 10k LOAs at a time
    public void insertLinesOfAccounting(List<LineOfAccounting> loas) throws SQLException {

        String sql = "INSERT INTO lines_of_accounting (id, loa_sys_id, loa_dpt_id, loa_tnsfr_dpt_nm, loa_baf_id, loa_trsy_sfx_tx, loa_maj_clm_nm, loa_op_agncy_id, loa_allt_sn_id, loa_pgm_elmnt_id, loa_tsk_bdgt_sbln_tx, loa_df_agncy_alctn_rcpnt_id, loa_jb_ord_nm, loa_sbaltmt_rcpnt_id, loa_wk_cntr_rcpnt_nm, loa_maj_rmbsmt_src_id, loa_dtl_rmbsmt_src_id, loa_cust_nm, loa_obj_cls_id, loa_srv_src_id, loa_spcl_intr_id, loa_bdgt_acnt_cls_nm, loa_doc_id, loa_cls_ref_id, loa_instl_acntg_act_id, loa_lcl_instl_id, loa_fms_trnsactn_id, loa_dsc_tx, loa_bgn_dt, loa_end_dt, loa_fnct_prs_nm, loa_stat_cd, loa_hist_stat_cd, loa_hs_gds_cd, org_grp_dfas_cd, loa_uic, loa_trnsn_id, loa_sub_acnt_id, loa_bet_cd, loa_fnd_ty_fg_cd, loa_bgt_ln_itm_id, loa_scrty_coop_impl_agnc_cd, loa_scrty_coop_dsgntr_cd, loa_scrty_coop_ln_itm_id, loa_agnc_dsbr_cd, loa_agnc_acntng_cd, loa_fnd_cntr_id, loa_cst_cntr_id, loa_prj_id, loa_actvty_id, loa_cst_cd, loa_wrk_ord_id, loa_fncl_ar_id, loa_scrty_coop_cust_cd, loa_end_fy_tx, loa_bg_fy_tx, loa_bgt_rstr_cd, loa_bgt_sub_act_cd, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int count = 0;

            for (LineOfAccounting loa : loas) {
                if (loa.getId() == null) {
                    loa.setId(UUID.randomUUID());
                }
                pstmt.setObject(1, loa.getId());
                pstmt.setString(2, loa.getLoaSysID());
                pstmt.setString(3, loa.getLoaDptID());
                pstmt.setString(4, loa.getLoaTnsfrDptNm());
                pstmt.setString(5, loa.getLoaBafID());
                pstmt.setString(6, loa.getLoaTrsySfxTx());
                pstmt.setString(7, loa.getLoaMajClmNm());
                pstmt.setString(8, loa.getLoaOpAgncyID());
                pstmt.setString(9, loa.getLoaAlltSnID());
                pstmt.setString(10, loa.getLoaPgmElmntID());
                pstmt.setString(11, loa.getLoaTskBdgtSblnTx());
                pstmt.setString(12, loa.getLoaDfAgncyAlctnRcpntID());
                pstmt.setString(13, loa.getLoaJbOrdNm());
                pstmt.setString(14, loa.getLoaSbaltmtRcpntID());
                pstmt.setString(15, loa.getLoaWkCntrRcpntNm());
                pstmt.setString(16, loa.getLoaMajRmbsmtSrcID());
                pstmt.setString(17, loa.getLoaDtlRmbsmtSrcID());
                pstmt.setString(18, loa.getLoaCustNm());
                pstmt.setString(19, loa.getLoaObjClsID());
                pstmt.setString(20, loa.getLoaSrvSrcID());
                pstmt.setString(21, loa.getLoaSpclIntrID());
                pstmt.setString(22, loa.getLoaBdgtAcntClsNm());
                pstmt.setString(23, loa.getLoaDocID());
                pstmt.setString(24, loa.getLoaClsRefID());
                pstmt.setString(25, loa.getLoaInstlAcntgActID());
                pstmt.setString(26, loa.getLoaLclInstlID());
                pstmt.setString(27, loa.getLoaFmsTrnsactnID());
                pstmt.setString(28, loa.getLoaDscTx());
                pstmt.setDate(29, java.sql.Date.valueOf(loa.getLoaBgnDt().toLocalDate()));
                pstmt.setDate(30, java.sql.Date.valueOf(loa.getLoaEndDt().toLocalDate()));
                pstmt.setString(31, loa.getLoaFnctPrsNm());
                pstmt.setString(32, loa.getLoaStatCd());
                pstmt.setString(33, loa.getLoaHistStatCd());
                pstmt.setString(34, loa.getLoaHsGdsCd());
                pstmt.setString(35, loa.getOrgGrpDfasCd());
                pstmt.setString(36, loa.getLoaUic());
                pstmt.setString(37, loa.getLoaTrnsnID());
                pstmt.setString(38, loa.getLoaSubAcntID());
                pstmt.setString(39, loa.getLoaBetCd());
                pstmt.setString(40, loa.getLoaFndTyFgCd());
                pstmt.setString(41, loa.getLoaBgtLnItmID());
                pstmt.setString(42, loa.getLoaScrtyCoopImplAgncCd());
                pstmt.setString(43, loa.getLoaScrtyCoopDsgntrCd());
                pstmt.setString(44, loa.getLoaScrtyCoopLnItmID());
                pstmt.setString(45, loa.getLoaAgncDsbrCd());
                pstmt.setString(46, loa.getLoaAgncAcntngCd());
                pstmt.setString(47, loa.getLoaFndCntrID());
                pstmt.setString(48, loa.getLoaCstCntrID());
                pstmt.setString(49, loa.getLoaPrjID());
                pstmt.setString(50, loa.getLoaActvtyID());
                pstmt.setString(51, loa.getLoaCstCd());
                pstmt.setString(52, loa.getLoaWrkOrdID());
                pstmt.setString(53, loa.getLoaFnclArID());
                pstmt.setString(54, loa.getLoaScrtyCoopCustCd());
                pstmt.setObject(55, loa.getLoaEndFyTx());
                pstmt.setObject(56, loa.getLoaBgFyTx());
                pstmt.setString(57, loa.getLoaBgtRstrCd());
                pstmt.setString(58, loa.getLoaBgtSubActCd());
                pstmt.setTimestamp(59, java.sql.Timestamp.valueOf(loa.getUpdatedAt()));
                pstmt.addBatch();

                // Execute every 10000 rows or when finished with the provided LOAs
                if (count++ % 10000 == 0 || count == loas.size()) {
                    pstmt.executeBatch();
                }
            }
        }
    }

}
