package com.opengamma.financial.securities;

public class EquityBean {
  private String _symbol;
  private String _name;
  private String _sector;
  private String _industry;
  private Double _pe;
  private Double _eps;
  private Double _divYield;
  private Double _shares;
  private Double _dps;
  private Double _peg;
  private Double _pts;
  private Double _ptb;
  
  public String getSymbol() {
    return _symbol;
  }
  public void setSymbol(String symbol) {
    _symbol = symbol;
  }
  public String getName() {
    return _name;
  }
  public void setName(String name) {
    _name = name;
  }
  public String getSector() {
    return _sector;
  }
  public void setSector(String sector) {
    _sector = sector;
  }
  public String getIndustry() {
    return _industry;
  }
  public void setIndustry(String industry) {
    _industry = industry;
  }
  public Double getPe() {
    return _pe;
  }
  public void setPe(Double pe) {
    _pe = pe;
  }
  public Double getEps() {
    return _eps;
  }
  public void setEps(Double eps) {
    _eps = eps;
  }
  public Double getDivYield() {
    return _divYield;
  }
  public void setDivYield(Double divYield) {
    _divYield = divYield;
  }
  public Double getShares() {
    return _shares;
  }
  public void setShares(Double shares) {
    _shares = shares;
  }
  public Double getDps() {
    return _dps;
  }
  public void setDps(Double dps) {
    _dps = dps;
  }
  public Double getPeg() {
    return _peg;
  }
  public void setPeg(Double peg) {
    _peg = peg;
  }
  public Double getPts() {
    return _pts;
  }
  public void setPts(Double pts) {
    _pts = pts;
  }
  public Double getPtb() {
    return _ptb;
  }
  public void setPtb(Double ptb) {
    _ptb = ptb;
  }
}
