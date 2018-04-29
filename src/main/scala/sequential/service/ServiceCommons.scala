package sequential.service

trait ServiceCommons {
  // TODO: Add any other attributes you want available to all services here
  val root = "sequential"
  val packageName = getClass.getPackage.getImplementationTitle
  val packageVersion = getClass.getPackage.getImplementationVersion

}