package poc.diff
package parser

import java.io.InputStream

import ujson.Value

import scala.collection.mutable

class DiffParserByDiffJson extends DiffParser {
  /**
   * 解析 diff，生成 diff-result
   *
   * @return
   */
  override def parse(in: InputStream): Seq[DiffFile] = {
    val json = ujson.read(in)
    json.arr.map(f = x => {
      val file = x.obj
      val newName = file("newName").str
      val oldName = file("oldName").str
      val language = file("language").str

      val isDeleted = file.get("isDeleted").flatMap(_.boolOpt)
      isDeleted match {
        case Some(true) => Deleted(newName, language)
        case _ =>
          val isNew = file.get("isNew").flatMap(_.boolOpt)
          isNew match {
            case Some(true) => Created(newName, language)
            case _ =>
              val changedLines = parseChangedLines(file)
              val isRename = file.get("isRename").flatMap(_.boolOpt)
              isRename match {
                case Some(true) => Rename(oldName, newName, language, changedLines)
                case _ => Changed(newName, language, changedLines)
              }
          }
      }
    }).toSeq
  }

  private def parseChangedLines(file: mutable.LinkedHashMap[String, Value]) = {
    file("blocks").arr
      .flatMap(block => {
        val newNumbers =
          block("lines").arr
            .filter(_.obj.get("type").exists(_.str != "context"))
            .map(_.obj.get("newNumber").map(_.num))
        val newStartLine = block.obj.get("newStartLine").map(_.num)
        newNumbers.addOne(newStartLine)
      })
      .flatten
      .toSet
  }
}