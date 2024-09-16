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
public class TransportationAccountingCodesHandler {
    private Logger logger = (Logger) LoggerFactory.getLogger(TransportationAccountingCodesHandler.class);

    private Trdm trdmUtil;

    public TransportationAccountingCodesHandler(Trdm trdmUtil) {
        this.trdmUtil = trdmUtil;
    }

    // This cron job will handle the entirety of ensuring the RDS db
    // is up to date with proper TGET data.
    public void tacCron()
            throws SQLException, DatatypeConfigurationException, TableRequestException, IOException, ParseException, URISyntaxException {
        // Gather the last update from TRDM
        logger.info("getting lastTableUpdate response with physical name TRNSPRTN_ACNT");
        LastTableUpdateResponse response = trdmUtil.LastTableUpdate("TRNSPRTN_ACNT");
        XMLGregorianCalendar trdmLastUpdate = response.getLastUpdate();
        logger.info("received LastTableUpdateResponse, getting our latest TGET update now");
        XMLGregorianCalendar ourLastUpdate = trdmUtil.GetOurLastTGETUpdate("transportation_accounting_codes");
        logger.info("received out latest TGET update. Comparing the 2 values to see if our TGET data is out of date");
        boolean tgetOutOfDate = trdmUtil.IsTGETDataOutOfDate(ourLastUpdate, trdmLastUpdate);
        if (tgetOutOfDate) {
            logger.info("TAC TGET data is out of date. Starting updateTGETData flow");
            trdmUtil.UpdateTGETData(ourLastUpdate, "TRNSPRTN_ACNT", "transportation_accounting_codes", trdmLastUpdate);
            logger.info("finished updating TAC TGET data");
        } else {
            // The data in RDS is up to date, no need to proceed
            logger.info("Transportation Accounting Codes RDS Table TGET data already up to date");
        }
    }
}
