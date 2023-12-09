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

        String sql = "INSERT INTO transportation_accounting_codes (id, tac, tac_sys_id, loa_sys_id, tac_fy_txt, tac_fn_bl_mod_cd, org_grp_dfas_cd, tac_mvt_dsg_id, tac_ty_cd, tac_use_cd, tac_maj_clmt_id, tac_bill_act_txt, tac_cost_ctr_nm, buic, tac_hist_cd, tac_stat_cd, trnsprtn_acnt_tx, trnsprtn_acnt_bgn_dt, trnsprtn_acnt_end_dt, dd_actvty_adrs_id, tac_blld_add_frst_ln_tx, tac_blld_add_scnd_ln_tx, tac_blld_add_thrd_ln_tx, tac_blld_add_frth_ln_tx, tac_fnct_poc_nm) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
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
                pstmt.addBatch();

                // Execute every 10000 rows or when finished with the provided TACs
                if (count++ % 10000 == 0 || count == codes.size()) {
                    pstmt.executeBatch();
                }
            }
        }
    }
}
