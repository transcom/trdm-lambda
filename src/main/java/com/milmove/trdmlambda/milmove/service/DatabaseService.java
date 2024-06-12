package com.milmove.trdmlambda.milmove.service;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.RdsUtilities;
import software.amazon.awssdk.services.rds.model.GenerateAuthenticationTokenRequest;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.milmove.trdmlambda.milmove.model.LineOfAccounting;
import com.milmove.trdmlambda.milmove.model.TransportationAccountingCode;
import com.milmove.trdmlambda.milmove.util.SecretFetcher;
import com.milmove.trdmlambda.milmove.constants.LinesOfAccountingDatabaseColumns;
import com.milmove.trdmlambda.milmove.constants.TransportationAccountingCodesDatabaseColumns;

import ch.qos.logback.classic.Logger;

// README: https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/java-rds.html
@Service
public class DatabaseService {

    private Logger logger = (Logger) LoggerFactory.getLogger(DatabaseService.class);

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

    // Batch update 10k TACs at a time
    public void updateTransportationAccountingCodes(List<TransportationAccountingCode> codes) throws SQLException {
        logger.info("updating Transportation Accounting Codes...");
        logger.info("updating " + codes.size() + " TAC(s)...");
        logger.info(codes.toString());


        String sql = "UPDATE transportation_accounting_codes SET tac=?, loa_id=?, loa_sys_id=?, tac_fy_txt=?, tac_fn_bl_mod_cd=?, org_grp_dfas_cd=?, tac_mvt_dsg_id=?, tac_ty_cd=?, tac_use_cd=?, tac_maj_clmt_id=?, tac_bill_act_txt=?, tac_cost_ctr_nm=?, buic=?, tac_hist_cd=?, tac_stat_cd=?, trnsprtn_acnt_tx=?, trnsprtn_acnt_bgn_dt=?, trnsprtn_acnt_end_dt=?, dd_actvty_adrs_id=?, tac_blld_add_frst_ln_tx=?, tac_blld_add_scnd_ln_tx=?, tac_blld_add_thrd_ln_tx=?, tac_blld_add_frth_ln_tx=?, tac_fnct_poc_nm=?, updated_at=? WHERE id=?";

        Connection conn = this.getConnection();
        conn.setAutoCommit(false);
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int count = 0;

            for (TransportationAccountingCode code : codes) {

                pstmt.setObject(1, code.getTac());
                pstmt.setObject(2, code.getLoaID());
                pstmt.setObject(3, code.getLoaSysID());
                pstmt.setObject(4, code.getTacFyTxt());
                pstmt.setObject(5, code.getTacFnBlModCd());
                pstmt.setObject(6, code.getOrgGrpDfasCd());
                pstmt.setObject(7, code.getTacMvtDsgID());
                pstmt.setObject(8, code.getTacTyCd());
                pstmt.setObject(9, code.getTacUseCd());
                pstmt.setObject(10, code.getTacMajClmtID());
                pstmt.setObject(11, code.getTacBillActTxt());
                pstmt.setObject(12, code.getTacCostCtrNm());
                pstmt.setObject(13, code.getBuic());
                pstmt.setObject(14, code.getTacHistCd());
                pstmt.setObject(15, code.getTacStatCd());
                pstmt.setObject(16, code.getTrnsprtnAcntTx());
                pstmt.setObject(17, code.getTrnsprtnAcntBgnDt());
                pstmt.setObject(18, code.getTrnsprtnAcntEndDt());
                pstmt.setObject(19, code.getDdActvtyAdrsID());
                pstmt.setObject(20, code.getTacBlldAddFrstLnTx());
                pstmt.setObject(21, code.getTacBlldAddScndLnTx());
                pstmt.setObject(22, code.getTacBlldAddThrdLnTx());
                pstmt.setObject(23, code.getTacBlldAddFrthLnTx());
                pstmt.setObject(24, code.getTacFnctPocNm());
                pstmt.setTimestamp(25, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setObject(26, code.getId());
                pstmt.addBatch();

                // Execute every 10000 rows or when finished with the provided TACs
                if (count++ % 10000 == 0 || count == codes.size()) {
                    pstmt.executeUpdate();
                }
            }
            conn.commit();
            logger.info("finished updating Transportation Accounting Codes...");

            List<UUID> codeIds = codes.stream().map(loa -> loa.getId()).collect(Collectors.toList());
            logger.info("updated the following TACs with Ids: \n" + codeIds.toString());
        } catch (SQLException e) {
            logger.info("failed updating Transportation Accounting Codes...");
            conn.rollback();
        }
    }

    // Batch insert 10k TACs at a time
    public void insertTransportationAccountingCodes(List<TransportationAccountingCode> codes) throws SQLException {
        logger.info("inserting Transportation Accounting Codes...");

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
            logger.info("finished inserting Transportation Accounting Codes...");
        }
    }

    // Batch update 10k LOAs at a time
    public void updateLinesOfAccountingCodes(List<LineOfAccounting> codes) throws SQLException {
        logger.info("updating Lines of Accounting Codes...");

        String sql = "UPDATE lines_of_accounting SET loa_dpt_id=?, loa_tnsfr_dpt_nm=?, loa_baf_id=?, loa_trsy_sfx_tx=?, loa_maj_clm_nm=?, loa_op_agncy_id=?, loa_allt_sn_id=?, loa_pgm_elmnt_id=?, loa_tsk_bdgt_sbln_tx=?, loa_df_agncy_alctn_rcpnt_id=?, loa_jb_ord_nm=?, loa_sbaltmt_rcpnt_id=?, loa_wk_cntr_rcpnt_nm=?, loa_maj_rmbsmt_src_id=?, loa_dtl_rmbsmt_src_id=?, loa_cust_nm=?, loa_obj_cls_id=?, loa_srv_src_id=?, loa_spcl_intr_id=?, loa_bdgt_acnt_cls_nm=?, loa_doc_id=?, loa_cls_ref_id=?, loa_instl_acntg_act_id=?, loa_lcl_instl_id=?, loa_fms_trnsactn_id=?, loa_dsc_tx=?,loa_bgn_dt=?, loa_end_dt=?, loa_fnct_prs_nm=?, loa_stat_cd=?, loa_hist_stat_cd=?, loa_hs_gds_cd=?, org_grp_dfas_cd=?, loa_uic=?, loa_trnsn_id=?, loa_sub_acnt_id=?, loa_bet_cd=?, loa_fnd_ty_fg_cd=?, loa_bgt_ln_itm_id=?, loa_scrty_coop_impl_agnc_cd=?, loa_scrty_coop_dsgntr_cd=?, loa_scrty_coop_ln_itm_id=?, loa_agnc_dsbr_cd=?, loa_agnc_acntng_cd=?, loa_fnd_cntr_id=?, loa_cst_cntr_id=?, loa_prj_id=?, loa_actvty_id=?, loa_cst_cd=?, loa_wrk_ord_id=?, loa_fncl_ar_id=?, loa_scrty_coop_cust_cd=?, loa_end_fy_tx=?, loa_bg_fy_tx=?, loa_bgt_rstr_cd=?, loa_bgt_sub_act_cd=?, updated_at=? WHERE id=?";

        Connection conn = this.getConnection();
        conn.setAutoCommit(false);

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int count = 0;

            for (LineOfAccounting code : codes) {

                pstmt.setObject(1, code.getLoaDptID());
                pstmt.setObject(2, code.getLoaTnsfrDptNm());
                pstmt.setObject(3, code.getLoaBafID());
                pstmt.setObject(4, code.getLoaTrsySfxTx());
                pstmt.setObject(5, code.getLoaMajClmNm());
                pstmt.setObject(6, code.getLoaOpAgncyID());
                pstmt.setObject(7, code.getLoaAlltSnID());
                pstmt.setObject(8, code.getLoaPgmElmntID());
                pstmt.setObject(9, code.getLoaTskBdgtSblnTx());
                pstmt.setObject(10, code.getLoaDfAgncyAlctnRcpntID());
                pstmt.setObject(11, code.getLoaJbOrdNm());
                pstmt.setObject(12, code.getLoaSbaltmtRcpntID());
                pstmt.setObject(13, code.getLoaWkCntrRcpntNm());
                pstmt.setObject(14, code.getLoaMajRmbsmtSrcID());
                pstmt.setObject(15, code.getLoaDtlRmbsmtSrcID());
                pstmt.setObject(16, code.getLoaCustNm());
                pstmt.setObject(17, code.getLoaObjClsID());
                pstmt.setObject(18, code.getLoaSrvSrcID());
                pstmt.setObject(19, code.getLoaSpclIntrID());
                pstmt.setObject(20, code.getLoaBdgtAcntClsNm());
                pstmt.setObject(21, code.getLoaDocID());
                pstmt.setObject(22, code.getLoaClsRefID());
                pstmt.setObject(23, code.getLoaInstlAcntgActID());
                pstmt.setObject(24, code.getLoaLclInstlID());
                pstmt.setObject(25, code.getLoaFmsTrnsactnID());
                pstmt.setObject(26, code.getLoaDscTx());
                pstmt.setObject(27, code.getLoaBgnDt());
                pstmt.setObject(28, code.getLoaEndDt());
                pstmt.setObject(29, code.getLoaFnctPrsNm());
                pstmt.setObject(30, code.getLoaStatCd());
                pstmt.setObject(31, code.getLoaHistStatCd());
                pstmt.setObject(32, code.getLoaHsGdsCd());
                pstmt.setObject(33, code.getOrgGrpDfasCd());
                pstmt.setObject(34, code.getLoaUic());
                pstmt.setObject(35, code.getLoaTrnsnID());
                pstmt.setObject(36, code.getLoaSubAcntID());
                pstmt.setObject(37, code.getLoaBetCd());
                pstmt.setObject(38, code.getLoaFndTyFgCd());
                pstmt.setObject(39, code.getLoaBgtLnItmID());
                pstmt.setObject(40, code.getLoaScrtyCoopImplAgncCd());
                pstmt.setObject(41, code.getLoaScrtyCoopDsgntrCd());
                pstmt.setObject(42, code.getLoaScrtyCoopLnItmID());
                pstmt.setObject(43, code.getLoaAgncDsbrCd());
                pstmt.setObject(44, code.getLoaAgncAcntngCd());
                pstmt.setObject(45, code.getLoaFndCntrID());
                pstmt.setObject(46, code.getLoaCstCntrID());
                pstmt.setObject(47, code.getLoaPrjID());
                pstmt.setObject(48, code.getLoaActvtyID());
                pstmt.setObject(49, code.getLoaCstCd());
                pstmt.setObject(50, code.getLoaWrkOrdID());
                pstmt.setObject(51, code.getLoaFnclArID());
                pstmt.setObject(52, code.getLoaScrtyCoopCustCd());
                pstmt.setObject(53, code.getLoaEndFyTx());
                pstmt.setObject(54, code.getLoaBgFyTx());
                pstmt.setObject(55, code.getLoaBgtRstrCd());
                pstmt.setObject(56, code.getLoaBgtSubActCd());
                pstmt.setTimestamp(57, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setObject(58, code.getId());


                pstmt.addBatch();

                // Execute every 10000 rows or when finished with the provided LOAs
                if (count++ % 10000 == 0 || count == codes.size()) {
                    pstmt.executeUpdate();
                }
            }
            conn.commit();

            logger.info("finished updating Lines of Accounting Codes...");

            List<UUID> codeIds = codes.stream().map(loa -> loa.getId()).collect(Collectors.toList());
            logger.info("updated the following TACs with Id: \n" + codeIds.toString());
        } catch (SQLException ex) {
            logger.info("failed to update Lines of Accounting Codes...");
            // Roll back update if fails
            conn.rollback();
        }
    }

    // Batch insert 10k LOAs at a time
    public void insertLinesOfAccounting(List<LineOfAccounting> loas) throws SQLException {

        logger.info("inserting Line of Accounting Codes...");

        String sql = "INSERT INTO lines_of_accounting (id, loa_sys_id, loa_dpt_id, loa_tnsfr_dpt_nm, loa_baf_id, loa_trsy_sfx_tx, loa_maj_clm_nm, loa_op_agncy_id, loa_allt_sn_id, loa_pgm_elmnt_id, loa_tsk_bdgt_sbln_tx, loa_df_agncy_alctn_rcpnt_id, loa_jb_ord_nm, loa_sbaltmt_rcpnt_id, loa_wk_cntr_rcpnt_nm, loa_maj_rmbsmt_src_id, loa_dtl_rmbsmt_src_id, loa_cust_nm, loa_obj_cls_id, loa_srv_src_id, loa_spcl_intr_id, loa_bdgt_acnt_cls_nm, loa_doc_id, loa_cls_ref_id, loa_instl_acntg_act_id, loa_lcl_instl_id, loa_fms_trnsactn_id, loa_dsc_tx, loa_bgn_dt, loa_end_dt, loa_fnct_prs_nm, loa_stat_cd, loa_hist_stat_cd, loa_hs_gds_cd, org_grp_dfas_cd, loa_uic, loa_trnsn_id, loa_sub_acnt_id, loa_bet_cd, loa_fnd_ty_fg_cd, loa_bgt_ln_itm_id, loa_scrty_coop_impl_agnc_cd, loa_scrty_coop_dsgntr_cd, loa_scrty_coop_ln_itm_id, loa_agnc_dsbr_cd, loa_agnc_acntng_cd, loa_fnd_cntr_id, loa_cst_cntr_id, loa_prj_id, loa_actvty_id, loa_cst_cd, loa_wrk_ord_id, loa_fncl_ar_id, loa_scrty_coop_cust_cd, loa_end_fy_tx, loa_bg_fy_tx, loa_bgt_rstr_cd, loa_bgt_sub_act_cd, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
                pstmt.setTimestamp(60, java.sql.Timestamp.valueOf(loa.getUpdatedAt()));
                pstmt.addBatch();

                // Execute every 10000 rows or when finished with the provided LOAs
                if (count++ % 10000 == 0 || count == loas.size()) {
                    pstmt.executeBatch();
                }
            }
            logger.info("finished inserting Line of Accounting Codes...");
        }
    }

    public ArrayList<LineOfAccounting> getAllLoas() throws SQLException {

        logger.info("retrieving all LOAs");

        ArrayList<LineOfAccounting> loas;

        // Select all loas
        String sql = "SELECT * FROM lines_of_accounting";

        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql);) {
            ResultSet rs = pstmt.executeQuery();
            loas = dbLoasToModel(rs);
        }

        logger.info("finished retrieving all LOAs");
        return loas;
    }

    public void deleteLoas(ArrayList<LineOfAccounting> loasToDelete) throws SQLException {
        logger.info("deleting LOAs");

        String sql = "DELETE FROM lines_of_accounting WHERE id=?";

        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql);) {

            int count = 0;

            for (LineOfAccounting lineOfAccounting : loasToDelete) {
                pstmt.setObject(1, lineOfAccounting.getId());
                pstmt.addBatch();
            }

            // Execute every 10000 rows or when finished with the provided LOAs
            if (count++ % 10000 == 0 || count == loasToDelete.size()) {
                pstmt.executeBatch();
            }
        }

        logger.info("finished deleting LOAs");
    }

    public ArrayList<LineOfAccounting> dbLoasToModel(ResultSet rs) throws SQLException {

        ArrayList<LineOfAccounting> loas = new ArrayList<LineOfAccounting>();

        // Loas created times are in 2 different formats. some have 25 characters and some have 26. Based on their character length will choose the formatter
        DateTimeFormatter timeFormatterLen25 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSS");
        DateTimeFormatter timeFormatterLen26 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

        while (rs.next()) {
            LineOfAccounting loa = new LineOfAccounting();
            loa.setId(UUID.fromString(rs.getString(LinesOfAccountingDatabaseColumns.id)));
            loa.setLoaSysID(rs.getString(LinesOfAccountingDatabaseColumns.loaSysId));
            loa.setLoaDptID(rs.getString(LinesOfAccountingDatabaseColumns.loaDptId));
            loa.setLoaTnsfrDptNm(rs.getString(LinesOfAccountingDatabaseColumns.loaTnsfrDptNm));
            loa.setLoaBafID(rs.getString(LinesOfAccountingDatabaseColumns.loaBafId));
            loa.setLoaTrsySfxTx(rs.getString(LinesOfAccountingDatabaseColumns.loaTrsySfxTx));
            loa.setLoaMajClmNm(rs.getString(LinesOfAccountingDatabaseColumns.loaMajClmNm));
            loa.setLoaOpAgncyID(rs.getString(LinesOfAccountingDatabaseColumns.loaOpAgncy_id));
            loa.setLoaAlltSnID(rs.getString(LinesOfAccountingDatabaseColumns.loaAlltSnId));
            loa.setLoaPgmElmntID(rs.getString(LinesOfAccountingDatabaseColumns.loaPgmElmntId));
            loa.setLoaTskBdgtSblnTx(rs.getString(LinesOfAccountingDatabaseColumns.loaTskBdgtSblnTx));
            loa.setLoaDfAgncyAlctnRcpntID(rs.getString(LinesOfAccountingDatabaseColumns.loaDfAgncyAlctnRcpntId));
            loa.setLoaJbOrdNm(rs.getString(LinesOfAccountingDatabaseColumns.loaJbOrdNm));
            loa.setLoaSbaltmtRcpntID(rs.getString(LinesOfAccountingDatabaseColumns.loaSbaltmtRcpntId));
            loa.setLoaWkCntrRcpntNm(rs.getString(LinesOfAccountingDatabaseColumns.loaWkCntrRcpntNm));
            loa.setLoaMajRmbsmtSrcID(rs.getString(LinesOfAccountingDatabaseColumns.loaMajRmbsmtSrcId));
            loa.setLoaDtlRmbsmtSrcID(rs.getString(LinesOfAccountingDatabaseColumns.loaDtlRmbsmtSrcId));
            loa.setLoaCustNm(rs.getString(LinesOfAccountingDatabaseColumns.loaCustNm));
            loa.setLoaObjClsID(rs.getString(LinesOfAccountingDatabaseColumns.loaObjClsId));
            loa.setLoaSrvSrcID(rs.getString(LinesOfAccountingDatabaseColumns.loaSrvSrcId));
            loa.setLoaSpclIntrID(rs.getString(LinesOfAccountingDatabaseColumns.loaSpclIntrId));
            loa.setLoaBdgtAcntClsNm(rs.getString(LinesOfAccountingDatabaseColumns.loaBdgtAcntClsNm));
            loa.setLoaDocID(rs.getString(LinesOfAccountingDatabaseColumns.loaDocId));
            loa.setLoaClsRefID(rs.getString(LinesOfAccountingDatabaseColumns.loaClsRefId));
            loa.setLoaInstlAcntgActID(rs.getString(LinesOfAccountingDatabaseColumns.loaInstlAcntgActId));
            loa.setLoaLclInstlID(rs.getString(LinesOfAccountingDatabaseColumns.loaLclInstlId));
            loa.setLoaFmsTrnsactnID(rs.getString(LinesOfAccountingDatabaseColumns.loaFmsTrnsactnId));
            loa.setLoaDscTx(rs.getString(LinesOfAccountingDatabaseColumns.loaDscTx));
            loa.setLoaFnctPrsNm(rs.getString(LinesOfAccountingDatabaseColumns.loaFnctPrsNm));
            loa.setLoaStatCd(rs.getString(LinesOfAccountingDatabaseColumns.loaStatCd));
            loa.setLoaHistStatCd(rs.getString(LinesOfAccountingDatabaseColumns.loaHistStatCd));
            loa.setLoaHsGdsCd(rs.getString(LinesOfAccountingDatabaseColumns.loaHsGdsCd));
            loa.setOrgGrpDfasCd(rs.getString(LinesOfAccountingDatabaseColumns.orgGrpDfasCd));
            loa.setLoaUic(rs.getString(LinesOfAccountingDatabaseColumns.loaUic));
            loa.setLoaTrnsnID(rs.getString(LinesOfAccountingDatabaseColumns.loaTrnsnId));
            loa.setLoaSubAcntID(rs.getString(LinesOfAccountingDatabaseColumns.loaSubAcntId));
            loa.setLoaBetCd(rs.getString(LinesOfAccountingDatabaseColumns.loaBetCd));
            loa.setLoaFndTyFgCd(rs.getString(LinesOfAccountingDatabaseColumns.loaFndTyFgCd));
            loa.setLoaBgtLnItmID(rs.getString(LinesOfAccountingDatabaseColumns.loaBgtLnItmId));
            loa.setLoaScrtyCoopImplAgncCd(rs.getString(LinesOfAccountingDatabaseColumns.loaScrtyCoopImplAgncCd));
            loa.setLoaScrtyCoopDsgntrCd(rs.getString(LinesOfAccountingDatabaseColumns.loaScrtyCoopDsgntrCd));
            loa.setLoaScrtyCoopLnItmID(rs.getString(LinesOfAccountingDatabaseColumns.loaScrtyCoopLnItmId));
            loa.setLoaAgncDsbrCd(rs.getString(LinesOfAccountingDatabaseColumns.loaAgncDsbrCd));
            loa.setLoaAgncAcntngCd(rs.getString(LinesOfAccountingDatabaseColumns.loaAgncAcntngCd));
            loa.setLoaFndCntrID(rs.getString(LinesOfAccountingDatabaseColumns.loaFndCntrId));
            loa.setLoaCstCntrID(rs.getString(LinesOfAccountingDatabaseColumns.loaCstCntrId));
            loa.setLoaPrjID(rs.getString(LinesOfAccountingDatabaseColumns.loaPrjId));
            loa.setLoaActvtyID(rs.getString(LinesOfAccountingDatabaseColumns.loaActvtyId));
            loa.setLoaCstCd(rs.getString(LinesOfAccountingDatabaseColumns.loaCstCd));
            loa.setLoaWrkOrdID(rs.getString(LinesOfAccountingDatabaseColumns.loaWrkOrdId));
            loa.setLoaFnclArID(rs.getString(LinesOfAccountingDatabaseColumns.loaFnclArId));
            loa.setLoaScrtyCoopCustCd(rs.getString(LinesOfAccountingDatabaseColumns.loaScrtyCoopCustCd));
            loa.setLoaEndFyTx(Integer.valueOf(rs.getString(LinesOfAccountingDatabaseColumns.loaEndFyTx)));
            loa.setLoaBgFyTx(Integer.valueOf(rs.getString(LinesOfAccountingDatabaseColumns.loaBgFyTx)));
            loa.setLoaBgtRstrCd(rs.getString(LinesOfAccountingDatabaseColumns.loaBgtRstrCd));
            loa.setLoaBgtSubActCd(rs.getString(LinesOfAccountingDatabaseColumns.loaBgtSubActCd));

            if (rs.getString(LinesOfAccountingDatabaseColumns.updatedAt).length() == 25) {
                loa.setUpdatedAt(LocalDateTime.parse(rs.getString(LinesOfAccountingDatabaseColumns.updatedAt), timeFormatterLen25));
            } else if (rs.getString(LinesOfAccountingDatabaseColumns.updatedAt).length() == 26) {
                loa.setUpdatedAt(LocalDateTime.parse(rs.getString(LinesOfAccountingDatabaseColumns.updatedAt), timeFormatterLen26));
            }

            loas.add(loa);
        }

        return loas;
    }

    public ArrayList<TransportationAccountingCode> getAllTacs() throws SQLException {

        logger.info("retrieving all TACs");

        ArrayList<TransportationAccountingCode> tacs = new ArrayList<TransportationAccountingCode>();

        // Select all TACs
        String sql = "SELECT * FROM transportation_accounting_codes;";

        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                TransportationAccountingCode tac = new TransportationAccountingCode();
                tac.setId(UUID.fromString(rs.getString(TransportationAccountingCodesDatabaseColumns.id)));
                tac.setTacSysID(rs.getString(TransportationAccountingCodesDatabaseColumns.tacSysId));
                tac.setLoaSysID(rs.getString(TransportationAccountingCodesDatabaseColumns.loaSysId));
                tac.setTac(rs.getString(TransportationAccountingCodesDatabaseColumns.tac));
                tac.setTacFyTxt(rs.getString(TransportationAccountingCodesDatabaseColumns.tacFnBlModCd));
                tac.setTacFnBlModCd(rs.getString(TransportationAccountingCodesDatabaseColumns.tacFnBlModCd));
                tac.setOrgGrpDfasCd(rs.getString(TransportationAccountingCodesDatabaseColumns.orgGrpDfasCd));
                tac.setTacTyCd(rs.getString(TransportationAccountingCodesDatabaseColumns.tacTyCd));
                tac.setTacUseCd(rs.getString(TransportationAccountingCodesDatabaseColumns.tacUseCd));
                tac.setTacMajClmtID(rs.getString(TransportationAccountingCodesDatabaseColumns.tacMajClmtId));
                tac.setTacCostCtrNm(rs.getString(TransportationAccountingCodesDatabaseColumns.tacCostCtrNm));
                tac.setTacStatCd(rs.getString(TransportationAccountingCodesDatabaseColumns.tacStatCd));
                tac.setTrnsprtnAcntTx(rs.getString(TransportationAccountingCodesDatabaseColumns.trnsprtnAcntTx));
                tac.setDdActvtyAdrsID(rs.getString(TransportationAccountingCodesDatabaseColumns.ddActvtyAdrsId));
                tac.setTacBlldAddFrstLnTx(
                        rs.getString(TransportationAccountingCodesDatabaseColumns.tacBlldAddFrstLnTx));
                tac.setTacBlldAddScndLnTx(
                        rs.getString(TransportationAccountingCodesDatabaseColumns.tacBlldAddScndLnTx));
                tac.setTacBlldAddThrdLnTx(
                        rs.getString(TransportationAccountingCodesDatabaseColumns.tacBlldAddThrdLnTx));
                tac.setTacBlldAddFrthLnTx(
                        rs.getString(TransportationAccountingCodesDatabaseColumns.tacBlldAddFrthLnTx));
                tac.setTacFnctPocNm(rs.getString(TransportationAccountingCodesDatabaseColumns.tacFnctPocNm));
                tac.setTacMvtDsgID(rs.getString(TransportationAccountingCodesDatabaseColumns.tacMvtDsgId));
                tac.setTacBillActTxt(rs.getString(TransportationAccountingCodesDatabaseColumns.tacBillActTxt));
                tac.setBuic(rs.getString(TransportationAccountingCodesDatabaseColumns.buic));
                tac.setTacHistCd(rs.getString(TransportationAccountingCodesDatabaseColumns.tacHistCd));

                tacs.add(tac);
            }

            logger.info("finished retrieving all TACs");
            return tacs;
        }

    }

    // Identify duplicate LOA.loaSysIds
    public ArrayList<LineOfAccounting> getDuplicateLoasToDelete() throws SQLException {

        logger.info(
                "identifying LOAs to delete based on loa records with a non unique loa_sys_id and not referenced in the transportation_accounting_codes table");

        // Select all duplicate loa_sys_id
        String sql = "Select * From lines_of_accounting Where loa_sys_id in (SELECT loa_sys_id FROM lines_of_accounting GROUP BY loa_sys_id HAVING COUNT(*) > 1) AND id NOT IN (SELECT loa_id FROM transportation_accounting_codes WHERE loa_id is NOT NULL)";

        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            ArrayList<LineOfAccounting> loas;
            loas = dbLoasToModel(rs);

            logger.info("finished identifying LOAs to delete");
            return loas;
        }
    }
}
