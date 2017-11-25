package com.whackdata

import java.nio.file._
import org.gdal.apps.ogr2ogr

import scala.io.StdIn
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Main extends App {

  def convertUsingOgr2Ogr(outPath: Path, cmd: Array[String]): Future[Path] = Future {
    println("Starting conversion using ogr2ogr")

    // Create the path to hold the file if it doesn't exist. Handy for keeping
    // Shapefiles organized in their own sub-folders.
    Files.createDirectories(outPath.toAbsolutePath.getParent)

    ogr2ogr.execute(cmd)

    // Return the path of the newly created file, out of convenience
    outPath
  }

  // Build the paths to the input and output files. You could also do this as
  // Paths.get("/some/path/to/a/file.gpkg") if you don't care about this working
  // across operating systems
  val inPath = Paths.get("src", "main", "resources", "Canada3573.gpkg").toAbsolutePath
  val outPathShp = Paths.get("temp", "Canada3573Shp", "Canada3573.shp").toAbsolutePath

  // Build your command, putting each part of the command in a separate string
  // stored as elements in an array
  val cmdShp = Array("-f", "ESRI Shapefile", outPathShp.toString, inPath.toString)

  // Execute the conversion asynchronously and print a success/failure message
  convertUsingOgr2Ogr(outPathShp, cmdShp).onComplete{
    case Success(outPath) => println("Shapefile conversion successful. File is located at " + outPath)
    case Failure(ex) => println("Shapefile conversion failed with error: " + ex.getMessage)
  }

  // If you need to add additional parameters, as is the case if creating a
  // CSV file, you can use the -lco flag multiple times
  val outPathCsv = Paths.get("temp", "Canada3573Csv", "Canada3573.csv").toAbsolutePath
  val cmdCsv = Array("-f", "CSV", outPathCsv.toString, inPath.toString,
    "-lco", "GEOMETRY=AS_WKT",
    "-lco", "CREATE_CSVT=YES",
    "-lco", "SEPARATOR=SEMICOLON"
  )

  convertUsingOgr2Ogr(outPathCsv, cmdCsv).onComplete{
    case Success(outPath) => println("Shapefile conversion successful. File is located at " + outPath)
    case Failure(ex) => println("Shapefile conversion failed with error: " + ex.getMessage)
  }

  // Block the main thread while the Futures run to prevent the JVM from exiting
  println(s"Running in background. Press Return to stop.")
  StdIn.readLine()

}
