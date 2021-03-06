/*
 * Copyright (c) 2019 - 2020 KazamaWataru
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tooko.pm

import java.util.*

class LongLongArrayMap : HashMap<Long, LinkedList<Long>>() {

    fun rawGet(key: Long): LinkedList<Long>? = super.get(key)

    override fun get(key: Long): LinkedList<Long> {

        var list = super.get(key)

        if (list == null) {

            list = LinkedList()

            put(key, list)

        }

        return list

    }

    fun countElements(): Int {

        var count = 0

        for (elements in values) {

            count += elements.size

        }

        return count
    }

}