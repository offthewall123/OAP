package org.apache.spark.sql.execution.datasources.oap.filecache;

import org.apache.arrow.plasma.PlasmaClient;

interface PlasmaLambdaWrapper {
    Object execute(PlasmaClient client, Object... param);
}

class PlasmaCreate implements PlasmaLambdaWrapper {

  @Override
  public Object execute(PlasmaClient client, Object... param) {
    return client.create((byte[])param[0], (int)param[1]);
  }
}

class PlasmaSeal implements PlasmaLambdaWrapper {

  @Override
  public Object execute(PlasmaClient client, Object... param) {
    client.seal((byte[])param[0]);
    return new Object();
  }
}

class PlasmaDelete implements PlasmaLambdaWrapper {

  @Override
  public Object execute(PlasmaClient client, Object... param) {
    client.delete((byte[])param[0]);
    return new Object();
  }
}

class PlasmaContains implements PlasmaLambdaWrapper {

  @Override
  public Object execute(PlasmaClient client, Object... param) {
    return client.contains((byte[])param[0]);
  }
}


class PlasmaGetObjAsByteBuffer implements PlasmaLambdaWrapper {

  @Override
  public Object execute(PlasmaClient client, Object... param) {
    return client
            .getObjAsByteBuffer((byte[])param[0], (int)param[1], (boolean)param[2]);
  }
}
