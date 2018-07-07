package no.fint.provider.student.generate

import spock.lang.Specification

class NavnSpec extends Specification {

    def "Can parse names properly"() {
        given:
        def names = new Navn()

        when:
        names.init()

        then:
        !names.etternavn.isEmpty()
        names.etternavn[0] == 'Hansen'

        then:
        !names.guttenavn.isEmpty()
        names.guttenavn[0] == 'Jakob'

        then:
        !names.jentenavn.isEmpty()
        names.jentenavn[0] == 'Sofie'
    }
}
