package mx.mk.explicits

import impl.GivenImplicit

import java.util.Properties
import scala.quoted.{Quotes, Type}

private[explicits]
trait CompilerBridge {
  /** Perform the implicit search */
  def search[T](using Quotes)(
    targetType:          Type[T],
    extraGivens:         Seq[GivenImplicit[?]],
    additionalLocations: Seq[Symbol],
    assistedFilter:      Type[?] => Boolean
  ): SearchResult[T]
}

private[explicits]
object CompilerBridge {
  private var _bridgeCache: CompilerBridge = _

  /** Get the bridge instance */
  def instance(using Quotes): CompilerBridge = {
    if (_bridgeCache == null) {
      _bridgeCache = loadBridge()
    }

    _bridgeCache
  }

  private def tryFindClass(name: String): Option[Class[?]] =
    try Some(getClass.getClassLoader.loadClass(name))
    catch {
      case _: NoClassDefFoundError | _: ClassNotFoundException => None
    }

  private def loadBridge()(using q: Quotes): CompilerBridge = {
    import q.reflect.report
    val props: Properties = new Properties()

    val res = getClass.getResourceAsStream("/compiler.properties")
    try props.load(res)
    finally res.close()

    val versionString = props.getProperty("version.number", "<undefined>")
    val versionParts = versionString.split("\\.")
    if (versionParts.length != 3 || versionParts(0) != "3") {
      throw new RuntimeException("Malformed version string: " + versionString)
    }

    val versionInt = versionParts(1).toInt * 1000 + versionParts(2).toInt
    val (selectedVersion, classNameShort) = bridgeClasses
      .reverseIterator
      .find { case (i, _) => versionInt >= i }
      .getOrElse(report.errorAndAbort("Failed to find compiler-specific implementation for assisted implicits for " +
        "compiler version: " + versionString))

    val className = "mx.mk.explicits." + classNameShort
    val clsObj = tryFindClass(className)
      .getOrElse(report.errorAndAbort("Failed to load implementation class: " + className))

    val settings = q.reflect.CompilationInfo.XmacroSettings
    if (settings.contains("mx.m-k.explicits.debug")) {
      val ownClassName = classOf[CompilerBridge].getName
      val vMajor = selectedVersion / 1000
      val vMinor = selectedVersion % 1000

      report.info(s"$ownClassName: compiler version $versionString, using bridge class '$className' " +
        s"(for version 3.$vMajor.$vMinor)")
    }

    clsObj
      .getConstructor()
      .newInstance()
      .asInstanceOf[CompilerBridge]
  }

  private val bridgeClasses: Seq[(Int, String)] = Seq(
    2000 -> "sc2.BridgeImpl",
    2001 -> "sc2_1.BridgeImpl",
    3000 -> "sc3.BridgeImpl",
    3001 -> "sc3_1.BridgeImpl",
    7000 -> "sc7.BridgeImpl",
  )
}
