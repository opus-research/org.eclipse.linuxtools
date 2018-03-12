package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.offset;

import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

public class OffsetMapContentProvider implements IStructuredContentProvider {

    Map<ITmfTrace, Long> fOffsetMap;

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    @SuppressWarnings("unchecked")
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) newInput;
            Object next = map.keySet().iterator().next();
            if ((next instanceof ITmfTrace) && (map.get(next) instanceof Long)) {
                fOffsetMap = (Map<ITmfTrace, Long>) newInput;
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) inputElement;
            Object next = map.keySet().iterator().next();
            if ((next instanceof ITmfTrace) && (map.get(next) instanceof Long)) {
                fOffsetMap = (Map<ITmfTrace, Long>) inputElement;
                return fOffsetMap.entrySet().toArray();
            }
        }
        return null;

    }

}
