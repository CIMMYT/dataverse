package edu.harvard.iq.dataverse.engine.command.impl;

import edu.harvard.iq.dataverse.ControlledVocabulary;
import edu.harvard.iq.dataverse.Dataverse;
import edu.harvard.iq.dataverse.DataverseControlledVocabulary;
import edu.harvard.iq.dataverse.authorization.Permission;
import edu.harvard.iq.dataverse.engine.command.AbstractVoidCommand;
import edu.harvard.iq.dataverse.engine.command.CommandContext;
import edu.harvard.iq.dataverse.engine.command.DataverseRequest;
import edu.harvard.iq.dataverse.engine.command.RequiredPermissions;
import edu.harvard.iq.dataverse.engine.command.exception.CommandException;


/**
 *
 * @author CIMMYT
 */

@RequiredPermissions( Permission.EditDataverse )
public class DeleteImportVocabularyCommand extends AbstractVoidCommand {

    private final Dataverse editedDv;
    private final DataverseControlledVocabulary dataverseControlledVocabulary;

    public DeleteImportVocabularyCommand(DataverseControlledVocabulary dvcv, DataverseRequest aRequest, Dataverse editedDv) {
        super(aRequest, editedDv);
        this.editedDv = editedDv;
        this.dataverseControlledVocabulary = dvcv;
    }

    @Override
    public void executeImpl(CommandContext ctxt) throws CommandException {
        ControlledVocabulary editedcv = ctxt.em().find(ControlledVocabulary.class, this.dataverseControlledVocabulary.getControlledVocabulary().getId());
        int setImport = editedcv.getImports();
        setImport -= 1;
        editedcv.setImports(setImport);
        ctxt.em().merge(editedcv);
        DataverseControlledVocabulary doomeddvcv = ctxt.em().merge(dataverseControlledVocabulary);
        ctxt.em().remove(doomeddvcv);
    }

}