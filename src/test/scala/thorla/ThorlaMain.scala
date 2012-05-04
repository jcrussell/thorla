package test.thorla

import thorla._

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers

class ThorlaMainSpec extends Spec with ShouldMatchers {

  describe("main") {
    it("should find ThorlaExample.foo") {
      assert(ThorlaMain.main(Array(":foo")) === 1)
    }
  }
}
