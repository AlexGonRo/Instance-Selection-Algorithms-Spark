package utils

class Option(val name: String, val description:String, val synopsis:String,
    val default:Any, val optionType:Int,val possibilities:Seq[String]=Seq.empty)
