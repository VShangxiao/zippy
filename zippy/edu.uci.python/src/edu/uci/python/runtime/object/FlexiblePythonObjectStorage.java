/*
 * Copyright (c) 2015, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.uci.python.runtime.object;

import java.util.*;

import com.oracle.truffle.api.*;

import edu.uci.python.runtime.*;
import edu.uci.python.runtime.standardtype.*;

public abstract class FlexiblePythonObjectStorage extends PythonObject {

    private final PythonOptions options;

    public FlexiblePythonObjectStorage(PythonClass pythonClass) {
        super(pythonClass);
        assert pythonClass.getInstanceObjectLayout() instanceof FlexibleObjectLayout;
        objectLayout = pythonClass.getInstanceObjectLayout();
        setStorageClassObjectLayout((FlexibleObjectLayout) objectLayout);
        assert verifyLayout();
        this.options = new PythonOptions();

        if (options.InstrumentObjectStorageAllocation) {
            PythonObjectAllocationInstrumentor.getInstance().instrumentFlexible(this);
        }
    }

    protected abstract FlexibleObjectLayout getStorageClassObjectLayout();

    protected abstract void setStorageClassObjectLayout(FlexibleObjectLayout layout);

    @Override
    public void syncObjectLayoutWithClass() {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        assert verifyLayout();
        FlexibleObjectLayout storageLayout = getStorageClassObjectLayout();
        assert storageLayout != null;

        if (objectLayout != storageLayout) {
            updateLayout(storageLayout);
        }

        assert verifyLayout();
        return;
    }

    @Override
    public void updateLayout(ObjectLayout newLayout) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        assert verifyLayout();

        // Get the current values of instance variables
        final Map<String, Object> instanceVariableMap = getAttributes();

        // Use new Layout
        assert ((FlexibleObjectLayout) objectLayout).getVersion() == ((FlexibleObjectLayout) newLayout).getVersion();
        objectLayout = newLayout;

        // Synchronize instance object layout with the storage class
        if (!usePrivateLayout) {
            setStorageClassObjectLayout((FlexibleObjectLayout) newLayout);

            if (!options.FlexibleObjectStorageEvolution && !pythonClass.getInstanceObjectLayout().getValidAssumption().isValid()) {
                pythonClass.updateInstanceObjectLayout(newLayout);
            }

            if (options.FlexibleObjectStorageEvolution && !pythonClass.getInstanceObjectLayout().getValidAssumption().isValid()) {
                FlexibleObjectLayout nu = (FlexibleObjectLayout) newLayout;
                FlexibleObjectLayout current = (FlexibleObjectLayout) pythonClass.getInstanceObjectLayout();

                if (nu.getVersion() >= current.getVersion()) {
                    pythonClass.updateInstanceObjectLayout(newLayout);
                }
            }
        }

        // Make all primitives as unset
        setPrimitiveSetMap(0);

        // Create a new array for objects
        allocateSpillArray();

        // Restore values
        setAttributes(instanceVariableMap);

        assert verifyLayout();
    }
}
