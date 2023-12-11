package com.milmove.trdmlambda.milmove.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LineOfAccounting {
    private UUID id; // RDS Internal
    private String loaSysID;
    private String loaDptID;
    private String loaTnsfrDptNm;
    private String loaBafID;
    private String loaTrsySfxTx;
    private String loaMajClmNm;
    private String loaOpAgncyID;
    private String loaAlltSnID;
    private String loaPgmElmntID;
    private String loaTskBdgtSblnTx;
    private String loaDfAgncyAlctnRcpntID;
    private String loaJbOrdNm;
    private String loaSbaltmtRcpntID;
    private String loaWkCntrRcpntNm;
    private String loaMajRmbsmtSrcID;
    private String loaDtlRmbsmtSrcID;
    private String loaCustNm;
    private String loaObjClsID;
    private String loaSrvSrcID;
    private String loaSpclIntrID;
    private String loaBdgtAcntClsNm;
    private String loaDocID;
    private String loaClsRefID;
    private String loaInstlAcntgActID;
    private String loaLclInstlID;
    private String loaFmsTrnsactnID;
    private String loaDscTx;
    private LocalDateTime loaBgnDt;
    private LocalDateTime loaEndDt;
    private String loaFnctPrsNm;
    private String loaStatCd;
    private String loaHistStatCd;
    private String loaHsGdsCd;
    private String orgGrpDfasCd;
    private String loaUic;
    private String loaTrnsnID;
    private String loaSubAcntID;
    private String loaBetCd;
    private String loaFndTyFgCd;
    private String loaBgtLnItmID;
    private String loaScrtyCoopImplAgncCd;
    private String loaScrtyCoopDsgntrCd;
    private String loaScrtyCoopLnItmID;
    private String loaAgncDsbrCd;
    private String loaAgncAcntngCd;
    private String loaFndCntrID;
    private String loaCstCntrID;
    private String loaPrjID;
    private String loaActvtyID;
    private String loaCstCd;
    private String loaWrkOrdID;
    private String loaFnclArID;
    private String loaScrtyCoopCustCd;
    private Integer loaEndFyTx;
    private Integer loaBgFyTx;
    private String loaBgtRstrCd;
    private String loaBgtSubActCd;
    private LocalDateTime createdAt; // RDS Internal
    private LocalDateTime updatedAt; // RDS Internal
}
