package test
import org.jetbrains.dokka.utilities.{DokkaConsoleLogger, DokkaLogger}
import testApi.logger.TestLogger
import org.jetbrains.dokka.testApi.testRunner._
import org.jetbrains.dokka.plugability.DokkaPlugin
import java.io.File
import dotty.tastydoc.representations
import dotty.tastydoc.representations._
import dotty.dokka.{DocConfiguration, DottyDokkaConfig}
import collection.JavaConverters._
import org.junit.rules.TemporaryFolder

abstract class DottyAbstractCoreTest extends AbstractCoreTest:
    val logger: DokkaLogger = new TestLogger(DokkaConsoleLogger.INSTANCE)

    def listTastyFiles(f: File): Seq[File] = 
      val (files, dirs) = f.listFiles().partition(_.isFile)
      files.filter(_.getName.endsWith(".tasty")) ++ dirs.flatMap(listTastyFiles)

    private def getTempDir() : TemporaryFolder =
        val folder = new TemporaryFolder()
        folder.create()
        folder

    def runTest(
        tastyDir: String,
        pluginOverrides: List[DokkaPlugin] = List[DokkaPlugin](),
        testingFunction: Function1[AbstractCoreTest$TestBuilder, Unit]
    ): Unit =
        val tests = new AbstractCoreTest$TestBuilder()
        testingFunction(tests)
        val config = new DottyDokkaConfig(
            DocConfiguration(
                tastyFiles = tastyDir.split(File.pathSeparatorChar).toList.flatMap(p => listTastyFiles(new File(p))).map(_.toString),
                classpath = System.getProperty("java.class.path")
            )
        )
        config._outputDir = getTempDir().getRoot.toPath.toAbsolutePath.toString
        DokkaTestGenerator(
            config,
            logger,
            tests.build(),
            pluginOverrides.asJava
        ).generate()
