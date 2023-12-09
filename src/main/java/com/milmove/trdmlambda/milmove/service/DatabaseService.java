package com.milmove.trdmlambda.milmove.service;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.RdsUtilities;
import software.amazon.awssdk.services.rds.model.GenerateAuthenticationTokenRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import com.milmove.trdmlambda.milmove.model.TransportationAccountingCode;
import com.milmove.trdmlambda.milmove.util.SecretFetcher;

// README: https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/java-rds.html
@Service
public class DatabaseService {

    // TODO: Reinstate
    // private String hostname;
    // private Integer port;
    // private String dbName;
    // private String username;
    private RdsClient rdsClient;

    // TODO: Remove spring @Value and replace with secret fetcher usage

    @Value("${myproperty.hostname}")
    private String hostname;

    @Value("${myproperty.port}")
    private int port;

    @Value("${myproperty.dbname}")
    private String dbName;

    @Value("${myproperty.username}")
    private String username;

    public DatabaseService(SecretFetcher secretFetcher) {
        // TODO: Reinstate
        // this.hostname = secretFetcher.getSecret("rds_hostname");
        // this.port = Integer.parseInt(secretFetcher.getSecret("rds_port"));
        // this.dbName = secretFetcher.getSecret("rds_db_name");
        // this.username = secretFetcher.getSecret("rds_username");
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
                .build();

        String authToken = utilities.generateAuthenticationToken(tokenRequest);
        String jdbcUrl = "jdbc:postgresql://"
                + hostname
                + ":" + port
                + "/" + dbName;

        return DriverManager.getConnection(jdbcUrl, username, authToken);
    }

    public void insertTransportationAccountingCodes(List<TransportationAccountingCode> codes) {
        // TODO: jdbc connection insert the TAC data into RDS db
    }
}
