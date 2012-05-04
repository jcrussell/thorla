package test.thorla

import thorla._

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers

class ThorlaSpec extends Spec with ShouldMatchers {

  describe("ThorlaExample") {
    it("should respond to example:foo") {
      assert(ThorlaExample.respondsTo("example:foo"))
    }
  }
}
