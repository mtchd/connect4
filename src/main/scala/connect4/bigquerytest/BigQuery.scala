package connect4.bigquerytest

object BigQuery {

  import bigquery4s._

  val bq = BigQuery() // expects $HOME/.bigquery/client_secret.json

  val ds = bq.listDatasets("publicdata")
  // ds: Seq[bigquery4s.WrappedDatasets] = ArrayBuffer(WrappedDatasets({"datasetReference":{"datasetId":"samples","projectId":"publicdata"},"id":"publicdata:samples","kind":"bigquery#dataset"}))

  val yourOwnProjectId = "CHANGE THIS"
  val jobId = bq.startQuery(yourOwnProjectId,
    "SELECT weight_pounds,state,year,gestation_weeks FROM publicdata:samples.natality ORDER BY weight_pounds DESC LIMIT 100")
  // jobId: bigquery4s.JobId = JobId(ProjectId(thinking-digit-98014),job_yzM_VroG0wbj1CIeLh4U4u6V1BI)

  val job = bq.await(jobId)
  // job: bigquery4s.WrappedCompletedJob = WrappedCompletedJob({"configuration":{"query":{"createDisposition":"CREATE_IF_NEEDED","destinationTable":{"datasetId":"_ce67ab3da040a9d26fa59261366d42efce66d7a2","projectId":"thinking-digit-98014","tableId":"anoncda8bb0f9e2201ba87406ed02b1681750920f9af"},"query":"SELECT weight_pounds,state,year,gestation_weeks FROM publicdata:samples.natality ORDER BY weight_pounds DESC LIMIT 100","writeDisposition":"WRITE_TRUNCATE"}},"etag":"\"Gn3Hpo5WaKnpFuT457VBDNMgZBw/8QF2L_W0fUFwBdI0pxm5gUIc6dw\"","id":"thinking-digit-98014:job_DgtpBcg85jewo3soMIwXDH0v-r8","jobReference":{"jobId":"job_DgtpBcg85jewo3soMIwXDH0v-r8","projectId":"thinking-digit-98014"},"kind":"bigquery#job","selfLink":"https://www.googleapis.com/bigquery/v2/projects/thinking-digit-98014/jobs/job_Dg...

  val rows = bq.getRows(job)
  // rows: Seq[bigquery4s.WrappedTableRow] = ArrayBuffer(WrappedTableRow({"f":[{"v":"18.0007436923"},{"v":null},{"v":"2005"},{"v":null}]}), WrappedTableRow({"f":[{"v":"18.0007436923"},{"v":null},{"v":"2005"},{"v":"37"}]}), WrappedTableRow({"f":[{"v":"18.0007436923"},{"v":"KY"},{"v":"2004"},{"v":"47"}]}), WrappedTableRow({"f":[{"v":"18.0007436923"},{"v":"WY"},{"v":"1979"},{"v":"35"}]}), WrappedTableRow({"f":[{"v":"18.0007436923"},{"v":null},{"v":"2006"},{"v":"39"}]}), WrappedTableRow({"f":[{"v":"18.0007436923"},{"v":null},{"v":"2006"},{"v":"37"}]}), WrappedTableRow({"f":[{"v":"18.0007436923"},{"v":"KY"},{"v":"2004"},{"v":"38"}]}), WrappedTableRow({"f":[{"v":"18.0007436923"},{"v":"KY"},{"v":"2004"},{"v":"39"}]}), WrappedTableRow({"f":[{"v":"18.0007436923"},{"v":"KY"},{"v":"2004"},{"v":"47"}]})...

  println(rows.take(10).map(_.cells.map(_.value.orNull).mkString(",")).mkString("\n"))
}
