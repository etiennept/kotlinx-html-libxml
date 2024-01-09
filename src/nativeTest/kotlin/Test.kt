import kotlinx.cinterop.*
import libcurl.*
import kotlin.test.Test


class Test {



    fun ee (aa : Appendable){

    }

    @OptIn(ExperimentalForeignApi::class)
    @Test
    fun test (){
        val doc = xmlNewDoc(xmlCharStrdup("1.0" )!! )!!

        val ns  = xmlNewNs(null ,   null  , xmlCharStrdup("h") )
        val element = xmlNewNode( ns  , xmlCharStrdup("hello")!!      ,  )!!
        //val  ns = xmlNewNs(element, xmlCharStrdup("h" )!!  , xmlCharStrdup("hello")   )
        //xmlSetNs( element , ns )
        xmlDocSetRootElement(doc,  element )
        val buffer = xmlBufferCreate()!!
        xmlNodeDump(  buffer ,doc , element , 0 , 1    )
        val  a = buffer.pointed.content!!
        println( a.reinterpret< ByteVar  >( ).toKString() )
    }


    @OptIn(ExperimentalForeignApi::class)
    fun write (doc : xmlDocPtr, element :xmlNodePtr): String {
        val buffer = xmlBufferCreate()!!
        xmlNodeDump(  buffer ,doc , element , 0 , 1    )
        return  buffer.pointed.content!!.reinterpret< ByteVar  >( ).toKString()

    }

    @OptIn(ExperimentalForeignApi::class)
    @Test
    fun test2 (){
        val doc = xmlNewDoc(xmlCharStrdup("1.0" )!! )!!
        //val ns  = xmlNewNs(null ,   null  , xmlCharStrdup("h") )
        val element = xmlNewNode( null   , xmlCharStrdup("hello")!!      ,  )!!
        //val  ns = xmlNewNs(element, xmlCharStrdup("h" )!!  , xmlCharStrdup("hello")   )
        //xmlSetNs( element , ns )
        xmlDocSetRootElement(doc,  element )
        xmlSetProp( element , xmlCharStrdup("title") , xmlCharStrdup("hello")  )
        println(write( doc , element  )  )

       // xmlHasProp( element , xmlCharStrdup("title")  )?.let { xmlRemoveProp(it  ) }

        println(write(doc , element ) )
    }

    @OptIn(ExperimentalForeignApi::class)
    @Test
    fun test3 (){
        val doc = htmlNewDoc( null , null   )

        xmlDocSetRootElement( doc , xmlNewNode( null   , xmlCharStrdup("html")!!)!!.also { html ->
            xmlAddChild( html ,  xmlNewNode ( null , xmlCharStrdup("head")!! )!!


            )
            xmlAddChild( html  , xmlNewNode( null , xmlCharStrdup("body")!! )!!.also { body ->
                xmlAddChild( body ,  xmlNewNode( null , xmlCharStrdup("script")!!)!!.also {script ->
                    xmlAddChild( script , xmlNewText( xmlCharStrdup("console.log(\"hello\" )" ) )   )
                }  )
            } )
        })
        htmlSaveFile("hello.html" , doc)





    }


}