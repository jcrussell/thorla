package test.thorla
package util

import thorla.util.Util
import org.scalatest.FunSpec

/**
 * Some basic sanity checks for utils
 */
class UtilSpec extends FunSpec {

  val arrInt = Array(23, 45, 6, 23, 123, 34, 234, 0, 0)
  val arrLong = Array(10091922L, 18550745L, 6247315L, 22912610L, 3730331L, 31808932L)

  describe("pack/unpack ints") {
    it("should get the same value, num = 1") {
      val num = 1
      assert(arrInt === Util.unpackInts(Util.pack(arrInt, num), num))
    }
    it("should get the same value, num = 4") {
      val num = 4
      assert(arrInt === Util.unpackInts(Util.pack(arrInt, num), num))
    }
  }

  describe("pack/unpack longs") {
    it("should get the same value, num = 4") {
      val num = 4
      assert(arrLong === Util.unpackLongs(Util.pack(arrLong, num), num))
    }
    it("should get the same value, num = 8") {
      val num = 8
      assert(arrLong === Util.unpackLongs(Util.pack(arrLong, num), num))
    }
  }

  describe("byteArray2Hex/hex2ByteArray") {
    it("should get the same value") {
      var bytes = arrInt.map(_.toByte)
      assert(bytes === Util.hex2ByteArray(Util.byteArray2Hex(bytes)))
    }
  }

  describe("basename") {
    it("should produce the basename - absolute") {
      assert("foo.bar" === Util.basename("/path/to/foo.bar"))
    }

    it("should produce the basename - relative") {
      assert("foo.bar" === Util.basename("path/to/foo.bar"))
    }

    it("should remove the extension - absolute") {
      assert("foo" === Util.basename("/path/to/foo.bar", ".bar"))
    }

    it("should remove the extension - relative") {
      assert("foo" === Util.basename("path/to/foo.bar", ".bar"))
    }
  }

  describe("md5Hex") {
    it("should get the same result as md5sum") {
      val file = "build.sbt"
      val md5sum = Util.eval("md5sum %s".format(file)).split(" ")(0)
      val md5 = Util.md5Hex(file)
      assert(md5sum === md5)
    }
  }

  describe("sha1Hex") {
    it("should get the same result as sha1sum") {
      val file = "build.sbt"
      val sha1sum = Util.eval("sha1sum %s".format(file)).split(" ")(0)
      val sha1 = Util.sha1Hex(file)
      assert(sha1sum === sha1)
    }
  }

  describe("sha256Hex") {
    it("should get the same result as sha256sum") {
      val file = "build.sbt"
      val sha256sum = Util.eval("sha256sum %s".format(file)).split(" ")(0)
      val sha256 = Util.sha256Hex(file)
      assert(sha256sum === sha256)
    }
  }
}
