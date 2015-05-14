package com.github.fm_jm.neuraltrends.optimization

import org.dyndns.delphyne.groovy.ast.threadlocal.ThreadLocal

@Singleton
class Placeholder {
    @ThreadLocal
    Map local = defaultMap()

    Map global = defaultMap()

    static Map defaultMap(String k=null){
        [:].withDefault Placeholder.&defaultMap
    }
}
