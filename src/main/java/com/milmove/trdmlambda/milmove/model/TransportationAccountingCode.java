package com.milmove.trdmlambda.milmove.model;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransportationAccountingCode {
    private UUID id;  // RDS Internal
    private String tac;
    private UUID loaID; // RDS Internal
    private String tacSysID;
    private String loaSysID;
    private String tacFyTxt;
    private String tacFnBlModCd;
    private String orgGrpDfasCd;
    private String tacMvtDsgID;
    private String tacTyCd;
    private String tacUseCd;
    private String tacMajClmtID;
    private String tacBillActTxt;
    private String tacCostCtrNm;
    private String buic;
    private String tacHistCd;
    private String tacStatCd;
    private String trnsprtnAcntTx;
    private LocalDateTime trnsprtnAcntBgnDt;
    private LocalDateTime trnsprtnAcntEndDt;
    private String ddActvtyAdrsID;
    private String tacBlldAddFrstLnTx;
    private String tacBlldAddScndLnTx;
    private String tacBlldAddThrdLnTx;
    private String tacBlldAddFrthLnTx;
    private String tacFnctPocNm;
    private LocalDateTime createdAt; // RDS Internal, UTC
    private LocalDateTime updatedAt; // RDS Internal, UTC
}
