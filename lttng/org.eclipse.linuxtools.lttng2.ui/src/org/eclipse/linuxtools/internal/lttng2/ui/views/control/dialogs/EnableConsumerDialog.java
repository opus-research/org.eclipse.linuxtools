package org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * @author ematkho
 * @since 2.0
 */
public class EnableConsumerDialog extends CreateSessionDialog implements IEnableConsumerDialog {

    /**
     * constructor
     * @param shell param
     */
    public EnableConsumerDialog(Shell shell) {
        super(shell);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        this.createConfigureStreamingComposite(parent);
        return super.createDialogArea(parent);
    }

    @Override
    public void setSessionName(String Name) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isKernelConsumer() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isUserspaceConsumer() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setUrl(String URL) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDataUrl(String URL) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setControlUrl(String URL) {
        // TODO Auto-generated method stub

    }

    @Override
    public void enable() {
        // TODO Auto-generated method stub

    }

}
