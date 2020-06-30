package poc.javaParser

import java.io.{File, FileInputStream}

import poc.domain.ParseResult._
import poc.domain.{BlockRange, ClassOrInterface, Method}
import utest._

object JavaGeneratorTest extends TestSuite {
  val generator = new JavaGenerator()

  val tests = Tests {
    "invalid file should failed" - {
      val file = new File("poc/test/resources/Invalid.java")
      val filePath = file.getAbsolutePath
      val res = generator.parse(new FileInputStream(file), filePath)

      assertMatch(res){
        case Left(Error(`filePath`, _)) =>
      }
    }

    'testParse - {
      val file = new File("poc/test/resources/MockJavaData.java")
      val filePath = file.getAbsolutePath
      val res = generator.parse(new FileInputStream(file), filePath)

      assert(res.isRight)

      val resSeq = res.toSeq.flatMap(_.result)
      val methodCount = resSeq.length
      assert(methodCount == 6)

      val mockJavaData = ClassOrInterface(filePath, BlockRange(5, 25), "poc.test.somepackage.MockJavaData")

      val m1 = resSeq(0)
      assertMatch(m1) {
        case Method(`mockJavaData`, _, BlockRange(6, 7)) =>
      }

      val m2 = resSeq(1)
      assertMatch(m2) {
        case Method(`mockJavaData`, _, BlockRange(9, 11)) =>
      }

      val m3 = resSeq(2)
      assertMatch(m3) {
        case Method(`mockJavaData`, _, BlockRange(13, 13)) =>
      }

      val m4 = resSeq(3)
      assertMatch(m4) {
        case Method(`mockJavaData`, "Optional<N> F4(Optional<N>)", BlockRange(16, 18)) =>
      }

      val m5 = resSeq(4)
      assertMatch(m5) {
        case Method(ClassOrInterface(`filePath`, BlockRange(20, 24), "poc.test.somepackage.MockJavaData.NestedClass"), _, BlockRange(21, 23)) =>
      }

      val m6 = resSeq(5)
      assertMatch(m6) {
        case Method(ClassOrInterface(`filePath`, BlockRange(27, 30), "poc.test.somepackage.AnotherTopLevelClass"), _, BlockRange(28, 29)) =>
      }
    }
  }
}
