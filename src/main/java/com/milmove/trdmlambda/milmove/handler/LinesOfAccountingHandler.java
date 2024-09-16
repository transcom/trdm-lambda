package com.milmove.trdmlambda.milmove.handler;

import com.milmove.trdmlambda.milmove.util.Trdm;

import ch.qos.logback.classic.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.ParseException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.milmove.trdmlambda.milmove.exceptions.TableRequestException;
import com.milmove.trdmlambda.milmove.model.lasttableupdate.LastTableUpdateResponse;

@Component
public class LinesOfAccountingHandler {
    private Logger logger = (Logger) LoggerFactory.getLogger(LinesOfAccountingHandler.class);

    private Trdm trdmUtil;

    public LinesOfAccountingHandler(Trdm trdmUtil) {
        this.trdmUtil = trdmUtil;
    }

    // This cron job will handle the entirety of ensuring the RDS db
    // is up to date with proper TGET data.
    public void loaCron()
            throws SQLException, DatatypeConfigurationException, TableRequestException, IOException, ParseException, URISyntaxException {
        // Gather the last update from TRDM
        logger.info("getting lastTableUpdate response with physical name LN_OF_ACCT");
        LastTableUpdateResponse response = trdmUtil.LastTableUpdate("LN_OF_ACCT");
        XMLGregorianCalendar trdmLastUpdate = response.getLastUpdate();
        logger.info("received LastTableUpdateResponse, getting our latest TGET update now");
        XMLGregorianCalendar ourLastUpdate = trdmUtil.GetOurLastTGETUpdate("lines_of_accounting");
        logger.info("received out latest TGET update. Comparing the 2 values to see if our TGET data is out of date");
        boolean tgetOutOfDate = trdmUtil.IsTGETDataOutOfDate(ourLastUpdate, trdmLastUpdate);
        if (tgetOutOfDate) {
            logger.info("LOA TGET data is out of date. Starting updateTGETData flow");
            trdmUtil.UpdateTGETData(ourLastUpdate, "LN_OF_ACCT", "lines_of_accounting", trdmLastUpdate);
            logger.info("finished updating LOA TGET data");
        } else {
            // The data in RDS is up to date, no need to proceed
            logger.info("Lines of Accounting RDS Table TGET data already up to date");
        }
    }
}
