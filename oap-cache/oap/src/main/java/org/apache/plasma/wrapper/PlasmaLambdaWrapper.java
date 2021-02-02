package org.apache.plasma.wrapper;

import org.apache.arrow.plasma.PlasmaClient;

interface PlasmaLambdaWrapper {
    Object execute(Object... param);
}

class PlasmaCreate implements PlasmaLambdaWrapper {

  @Override
  public Object execute(Object... param) {
    return ((PlasmaClient)param[0]).create((byte[])param[1], (int)param[2]);
  }
}

class PlasmaSeal implements PlasmaLambdaWrapper {

  @Override
  public Object execute(Object... param) {
    ((PlasmaClient)param[0]).seal((byte[])param[1]);
    return new Object();
  }
}

class PlasmaDelete implements PlasmaLambdaWrapper {

  @Override
  public Object execute(Object... param) {
    ((PlasmaClient)param[0]).delete((byte[])param[1]);
    return new Object();
  }
}

class PlasmaContains implements PlasmaLambdaWrapper {

  @Override
  public Object execute(Object... param) {
    return ((PlasmaClient)param[0]).contains((byte[])param[1]);
  }
}


class PlasmaGetObjAsByteBuffer implements PlasmaLambdaWrapper {

  @Override
  public Object execute(Object... param) {
    return ((PlasmaClient)param[0])
            .getObjAsByteBuffer((byte[])param[1], (int)param[2], (boolean)param[3]);
  }
}
