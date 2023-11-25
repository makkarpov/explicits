import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.zip.{ZipEntry, ZipInputStream, ZipOutputStream}

object Packager {
  def combineJars(out: File, jars: File*): File = {
    val sOut = Files.newOutputStream(out.toPath)
    try {
      val zos = new ZipOutputStream(sOut, StandardCharsets.UTF_8)

      for ((jar, i) <- jars.zipWithIndex) {
        val sIn = Files.newInputStream(jar.toPath)
        try {
          val zis = new ZipInputStream(sIn, StandardCharsets.UTF_8)
          for (entry <- Iterator.continually(zis.getNextEntry).takeWhile(_ != null)) {
            val keep = !entry.isDirectory && (i == 0 || !entry.getName.startsWith("META-INF/"))

            if (keep) {
              zos.putNextEntry(new ZipEntry(entry.getName))
              zis.transferTo(zos)
              zos.closeEntry()
            }
          }
        } finally sIn.close()
      }

      zos.close()
    } finally sOut.close()

    out
  }
}
