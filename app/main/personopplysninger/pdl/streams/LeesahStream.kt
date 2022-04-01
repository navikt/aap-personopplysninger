package personopplysninger.pdl.streams

//
//internal class LeesahStream(
//    private val pdl: PdlGraphQLClient,
//    personopplysninger: KTable<String, Personopplysninger>,
//    kStreams: StreamsBuilder,
//) {
//    init {
//        kStreams
//            .consume(Topics.leesah)
//            .filterNotNull { "skip-leesah-tombstone" }
//            .filter(::isAdressebeskyttelse) { "filter-is-adressebeskyttelse" }
//            .selectKey("") { _, value -> value.personidenter.single { it.length == 11 } }
//            .join(Topics.leesah with Topics.personopplysninger, personopplysninger, Wrapper::merge)
//            .mapValues(::toAddressebeskyttelse)
//            .produce(Topics.personopplysninger) { "person-with-adressebeskyttelse" }
//    }
//
//    // https://github.com/navikt/pdl/blob/master/libs/contract-pdl-avro/src/main/java/no/nav/person/identhendelse/Opplysningstype.java
//    private fun isAdressebeskyttelse(k_: String, v: GenericRecord) = v.opplysningstype == "ADRESSEBESKYTTELSE_V1"
//    private fun toAddressebeskyttelse(person: Wrapper): Personopplysninger = TODO()
//
//    private class Wrapper(val leesah: GenericRecord, val person: Personopplysninger) {
//        companion object {
//            fun merge(left: GenericRecord, right: Personopplysninger) = Wrapper(left, right)
//        }
//    }
//}
//
//private val GenericRecord.opplysningstype get() = string("opplysningstype")
//private val GenericRecord.personidenter get() = array("personidenter").map(Any::toString)
//private val GenericRecord.gradering get() = generic("adressebeskyttelse")?.string("gradering")
