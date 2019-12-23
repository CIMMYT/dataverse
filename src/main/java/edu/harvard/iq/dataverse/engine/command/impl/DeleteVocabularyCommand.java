/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dataverse.engine.command.impl;

import edu.harvard.iq.dataverse.Dataverse;
import edu.harvard.iq.dataverse.DataverseControlledVocabulary;
import edu.harvard.iq.dataverse.ControlledVocabulary;
import edu.harvard.iq.dataverse.ControlledVocabularyTerms;
import edu.harvard.iq.dataverse.Template;
import edu.harvard.iq.dataverse.authorization.Permission;
import edu.harvard.iq.dataverse.engine.command.*;
import edu.harvard.iq.dataverse.engine.command.exception.CommandException;

import java.util.List;

/**
 *
 * @author CIMMYT
 */
@RequiredPermissions( Permission.EditDataverse )
public class DeleteVocabularyCommand extends AbstractVoidCommand {

    private final Dataverse editedDv;
    private final DataverseControlledVocabulary dataverseControlledVocabulary;

    public DeleteVocabularyCommand(DataverseControlledVocabulary dvcv, DataverseRequest aRequest, Dataverse editedDv) {
        super(aRequest, editedDv);
        this.editedDv = editedDv;
        this.dataverseControlledVocabulary = dvcv;
    }

    @Override
    public void executeImpl(CommandContext ctxt) throws CommandException {
        ControlledVocabulary cvd = dataverseControlledVocabulary.getControlledVocabulary();
        ControlledVocabulary doomedAndMerged = ctxt.em().merge(cvd);
        DataverseControlledVocabulary doomeddvcv = ctxt.em().merge(dataverseControlledVocabulary);
        ctxt.em().remove(doomeddvcv);
        ctxt.em().remove(doomedAndMerged);
    }

}
