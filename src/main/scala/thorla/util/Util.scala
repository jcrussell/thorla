package thorla
package util

import java.io.{EOFException, File, FileInputStream, InputStream}
import java.security.{DigestInputStream, MessageDigest}
import java.util.{Scanner}
import java.sql.PreparedStatement

/**
 * Utility class with helpful methods
 */
object Util {

  /**
   * Given the path to a file, return the file name portion
   */
  def basename(path: String): String = {
    basename(path, "")
  }

  /**
   * Given the path to a file, return the file name minus the extension.
   */
  def basename(path: String, ext: String): String = {
    new File(path).getName.replace(ext, "")
  }

  /**
   * Compute the md5 of a file, returns a byte array for the digest
   */
  def md5(file: String): Array[Byte] = {
    doDigest(MessageDigest.getInstance("MD5"), file)
  }

  /**
   * Compute the md5 of a file, returns the hex representation
   */
  def md5Hex(file: String): String = {
    byteArray2Hex(md5(file))
  }

  /**
   * Compute the sha1 of a file, returns a byte array for the digest
   */
  def sha1(file: String): Array[Byte] = {
    doDigest(MessageDigest.getInstance("SHA-1"), file)
  }

  /**
   * Compute the sha1 of a file, returns the hex representation
   */
  def sha1Hex(file: String): String = {
    byteArray2Hex(sha1(file))
  }

  /**
   * Compute the sha256 of a file, returns a byte array for the digest
   */
  def sha256(file: String): Array[Byte] = {
    doDigest(MessageDigest.getInstance("SHA-256"), file)
  }

  /**
   * Compute the sha1 of a file, returns the hex representation
   */
  def sha256Hex(file: String): String = {
    byteArray2Hex(sha256(file))
  }

  /**
   * Helper to compute the digest of a file
   */
  private def doDigest(digester: MessageDigest, file: String): Array[Byte] = {
    val stream = new DigestInputStream(new FileInputStream(file), digester)
    while(stream.read() != -1) {}
    stream.getMessageDigest.digest
  }

  /**
   * Converts a byte array to it's hex representation
   */
  def byteArray2Hex(hash: Array[Byte]): String = {
    hash.map("%02x".format(_)).mkString
  }

  /**
   * Converts a hex string to a byte array
   */
  def hex2ByteArray(hash: String): Array[Byte] = {
    hash.grouped(2).map{x => Integer.parseInt(x, 16).toByte }.toArray
  }

  /**
   * Get the current working directory as a File
   */
  def cwd(): File = {
    new File(System.getProperty("user.dir"))
  }

  def deleteRecursively(file: File, needsConfirm: Boolean = true) {
    if(!needsConfirm || confirm("recursively remove %s".format(file.getAbsolutePath))) {
      if(file.isDirectory) {
        Option(file.listFiles) match {
          case Some(files) => {
            files.foreach(deleteRecursively(_, false))
          }
          case None => { }
        }
      }

      file.delete()
    }
  }

  /**
   * Read the whole contents of a file into a String
   */
  def readFile(file: String): String = readFile(new File(file))
  def readFile(file: File) = readLines(file).mkString("\n")

  /**
   * Iterate over lines of a file
   */
  def readLines(file: String): Iterator[String] = readLines(new File(file))
  def readLines(file: File): Iterator[String] = readLines(new Scanner(file))
  def readLines(in: InputStream): Iterator[String] = readLines(new Scanner(in))
  def readLines(in: Scanner) = {
    new Iterator[String] {
      val scanner = in
      var closed = false
      override def hasNext = !closed && scanner.hasNextLine
      override def next = {
        val line = scanner.nextLine

        // Stop leak of file descriptor
        if(!hasNext) {
          scanner.close
          closed = true
        }

        line
      }
    }
  }

  /**
   * Iterate over lines of a resource (resource needs to be in classpath)
   */
  def readLinesFromResource(name: String): Iterator[String] = {
    readLines(new Scanner(ClassLoader.getSystemResourceAsStream(name)))
  }

  /**
   * Check if we should append an "s" based on a number, useful for printing
   * strings like: "# widget(s?)"
   */
  def pluralExt(i: Int): String = pluralExt(i.toDouble)
  def pluralExt(i: Double) = {
    if(i > 1 || i == 0) { "s" } else { "" }
  }

  /**
   * Take an array of ints and pack them into an array of bytes using num bytes per int
   * Note: may cause loss of precision if too few bytes are used per int
   */
  def pack(vec: Array[Int], num: Int): Array[Byte] = {
    pack(vec.map(_.toLong), num)
  }

  /**
   * Take an array of longs and pack them into an array of bytes using num bytes per long
   * Note: may cause loss of precision if too few bytes are used per long
   */
  def pack(vec: Array[Long], num: Int): Array[Byte] = {
    vec.flatMap{x => (0 until num).map{i => ((x >> 8*i) & 0xFF).toByte}}.toArray
  }

  /**
   * Take an array bytes and unpack them into an array of ints using num bytes per int
   */
  def unpackInts(bytes: Array[Byte], num: Int): Array[Int] = {
    unpackLongs(bytes, num).map(_.toInt)
  }

  /**
   * Take an array bytes and unpack them into an array of longs using num bytes per long
   */
  def unpackLongs(bytes: Array[Byte], num: Int): Array[Long] = {
    bytes.grouped(num).map{x =>
      (0 until num).map{i => (x(i).toLong & 0xFF) << 8*i}.toArray.sum
    }.toArray
  }

  /**
  * Runs a command in a shell and returns the output.
  */
  def eval(cmd: String): String = {
    readLines(Runtime.getRuntime.exec(cmd).getInputStream).mkString("\n")
  }

  def eval(cmd: Array[Any]): String = {
    eval(cmd.map(_.toString).mkString)
  }

  /**
   * Display a progress bar curr/total percent completed
   */
  def displayProgress(curr: Int, total: Int) {
    val cols = eval("tput cols").trim.toInt-2 // get terminal width in linux
    val nums = "%d/%d".format(curr, total)
    val progress = ((curr.toDouble/total)*cols).toInt
    val num_equals = Array(progress, cols-nums.length).min
    val num_spaces = Array(Array(cols-progress-nums.length, cols-nums.length).min, 0).max
    print("\r[%s%s%s]".format("=" * num_equals, " " * num_spaces, nums))
  }

  /**
   * Compute the standard deviation of an array of doubles
   */
  def stdDev(arr: Array[Double], avg: Double) = {
    def sqrdDiff(v1: Double, v2: Double) = math.pow(v1 - v2, 2.0)
    if(arr.size == 0) {
      0.0
    }
    else {
      val squared = arr.foldLeft(0.0)(_ + sqrdDiff(_, avg))
      math.sqrt(squared / arr.size)
    }
  }

  /**
   * Compute (min, max, avg, std dev) for ints
   */
  def printStats(name: String, iter: Iterable[Int]) {
    printStats(name, iter.map(_.toDouble).toArray)
  }

  /**
   * Compute (min, max, avg, std dev) for doubles
   */
  def printStats(name: String, arr: Array[Double]) {
    val min = arr.min
    val max = arr.max
    val avg = arr.foldLeft(0.0)(_+_) / arr.size
    val std = stdDev(arr, avg)
    print("Stats for %s:\n".format(name))
    print("\tmin: %.2f\n".format(min))
    print("\tmax: %.2f\n".format(max))
    print("\tavg: %.2f\n".format(avg))
    print("\tstd dev: %.2f\n".format(std))
  }

  /**
  * Takes a time in milli seconds and converts it to a nice string
  */
  def timeToStr(millis: Long) = {
    var x = millis/1000
    val hours = x/3600; x -= hours*3600
    val mins = x/60; x -= mins*60
    val secs = x

    var res = (if(hours > 0) { "%d hour%s, ".format(hours, pluralExt(hours)) } else { "" })
    res += (if(mins > 0) { "%d minute%s, ".format(mins, pluralExt(mins)) } else { "" })
    res += (if(mins > 0 || hours > 0) { "and " } else { "" })
    res += "%d second%s.".format(secs, pluralExt(secs))

    res
  }

  /**
   * Check whether the user really wants to do something
   */
  def confirm(op: String): Boolean = {
    print("Are you sure you want to %s? [y/N]: ".format(op))
    try {
      val char = readChar
      (char == 'y') || (char == 'Y')
    }
    catch {
      case e: EOFException => {
        println // User enter a Ctrl-d, print a new line
        false
      }
      case e: StringIndexOutOfBoundsException => {
        false
      }
    }
  }

  /**
   * Calculate the percentage of memory remaining
   */
  def memRemaining(): Double = {
    val runTime = Runtime.getRuntime()
    val used = (runTime.totalMemory - runTime.freeMemory).toDouble
    return 1-used/runTime.maxMemory
  }

  /**
   * Inverts a mapping
   */
  def invert[X, Y](map: Map[X, Iterable[Y]]): Map[Y, Set[X]] = {
    var inverted = Map[Y, Set[X]]()
    map.foreach{case (key, values) => {
      values.foreach(value => {
        inverted += (value -> (inverted.getOrElse(value, Set[X]()) + key))
      })
    }}

    inverted
  }
}
