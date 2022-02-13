package com.pi4j.io.group.impl;

/*-
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: LIBRARY  :: Java Library (CORE)
 * FILENAME      :  GroupBase.java
 *
 * This file is part of the Pi4J project. More information about
 * this project can be found here:  https://pi4j.com/
 * **********************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.pi4j.io.group.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class GroupBase<GROUP_TYPE extends Group, MEMBER_TYPE> implements Group<GROUP_TYPE, MEMBER_TYPE> {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    protected boolean state = false;
    protected Set<MEMBER_TYPE> members = new CopyOnWriteArraySet<>();

    public GroupBase(MEMBER_TYPE ... member){
        add(member);
    }

    @Override
    public GROUP_TYPE add(MEMBER_TYPE... member) {
//        members.addAll(List.of(member));
        return (GROUP_TYPE)this;
    }

    @Override
    public GROUP_TYPE remove(MEMBER_TYPE... member) {
//        members.removeAll(List.of(members));
        return (GROUP_TYPE)this;
    }

    @Override
    public GROUP_TYPE removeAll() {
        members.clear();
        return (GROUP_TYPE)this;
    }

    @Override
    public Collection<MEMBER_TYPE> members() {
        return Collections.unmodifiableSet(members);
    }
}
