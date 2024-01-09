package kotlinx.html

import kotlinx.cinterop.*
import kotlinx.html.org.w3c.dom.events.Event
import libcurl.*

@OptIn(ExperimentalForeignApi::class)
class LibxmlBuilder( val doc : xmlDocPtr   ) : TagConsumer<xmlNodePtr>  {
    private val path = arrayListOf<xmlNodePtr>()
    private var latest = null as xmlNodePtr?

    override fun onTagStart(tag: Tag) {
        val ns = tag.namespace?.let { xmlNewNs( null , null  , xmlCharStrdup(it ) )!!  }
        val nodePtr = xmlNewNode(  ns  , xmlCharStrdup(tag.tagName)!!  )!!
        tag.attributesEntries.forEach {
            xmlSetProp(nodePtr, xmlCharStrdup(it.key)!! , xmlCharStrdup(it.value)!! )!!
        }
        if( path.isNotEmpty()   ){
            xmlAddChild( path.last() , nodePtr )!!
        }
        path.add( nodePtr )

    }
    override fun onTagAttributeChange(tag: Tag, attribute: String, value: String?) {
        if (path.isEmpty()) {
            throw IllegalStateException("No current tag")
        }
        path.last().let { node ->
            value?.let { value  ->
                xmlSetProp(node, xmlCharStrdup(attribute),  xmlCharStrdup(value ))
            } ?:  xmlHasProp(node , xmlCharStrdup(attribute))?.let { xmlRemoveProp(it) }
        }
    }

    override fun onTagComment(content: CharSequence) {
        if (path.isEmpty()) {
            throw IllegalStateException("No current tag")
        }
        xmlAddChild(  path.last() ,  xmlNewComment(xmlCharStrdup(content.toString())!!)!!)!!
    }

    override fun onTagContent(content: CharSequence) {
        if (path.isEmpty()) {
            throw IllegalStateException("No current DOM node")
        }
            xmlAddChild(path.last() ,  xmlNewText(xmlCharStrdup( content.toString())!! )!! )!!
    }

    override fun onTagContentEntity(entity: Entities) {
        if (path.isEmpty()) {
            throw IllegalStateException("No current DOM node")
        }
        xmlAddChild(path.last()  ,  xmlNewCharRef( null , xmlCharStrdup(entity.name))!! )!!
    }

    override fun onTagContentUnsafe(block: Unsafe.() -> Unit) {
        unsafe.block()
    }


    private val unsafe = object : Unsafe {
        override fun String.unaryPlus() {
            val value = "<unsafeRoot>$this</unsafeRoot>"
            val node = xmlReadMemory( value ,value.length  ,null ,  null ,0  )!!.let {
                xmlDocCopyNode( xmlDocGetRootElement(  it  )  , doc  , 1 )!!
            }
            xmlAddChildList( path.last(), node.pointed.children)
        }
    }
    override fun onTagEnd(tag: Tag) {
        if (path.isEmpty() ||  path.last().pointed.name!!.reinterpret<ByteVar>().toKString().lowercase() != tag.tagName.lowercase()  ) {
            throw IllegalStateException("We haven't entered tag ${tag.tagName} but trying to leave")
        }
        val element = path.removeAt(path.lastIndex)
        latest =  element
    }

    override fun onTagEvent(tag: Tag, event: String, value: (Event) -> Unit) {
        throw UnsupportedOperationException("You can't assign lambda event handler on Native")
    }
    override fun finalize(): xmlNodePtr = latest ?: throw IllegalStateException("No tags were emitted")
}