package edu.harvard.iq.dataverse.engine.command.impl;

import edu.harvard.iq.dataverse.*;
import edu.harvard.iq.dataverse.authorization.Permission;
import edu.harvard.iq.dataverse.engine.command.AbstractVoidCommand;
import edu.harvard.iq.dataverse.engine.command.CommandContext;
import edu.harvard.iq.dataverse.engine.command.DataverseRequest;
import edu.harvard.iq.dataverse.engine.command.RequiredPermissions;
import edu.harvard.iq.dataverse.engine.command.exception.CommandException;

import java.util.List;

/**
 *
 * @author CIMMYT
 */
@RequiredPermissions( Permission.EditDataverse )

public class EditVocabularyCommand extends AbstractVoidCommand {
    private final ControlledVocabulary updateControlledVocabulary;
    private final List<ControlledVocabularyTerms> controlledVocabularyTerms;
    private final List<ControlledVocabularyTerms> deleteControlledVocabulary;
    private final Dataverse dv;

    public EditVocabularyCommand(ControlledVocabulary updatedcv, List<ControlledVocabularyTerms> newcvTerms, List<ControlledVocabularyTerms> deletecvTerms, DataverseRequest aRequest,  Dataverse anAffectedDataverse){
        super(aRequest, anAffectedDataverse);
        this.updateControlledVocabulary = updatedcv;
        this.controlledVocabularyTerms = newcvTerms;
        this.deleteControlledVocabulary = deletecvTerms;
        this.dv = anAffectedDataverse;
    }

    @Override
    public void executeImpl(CommandContext ctxt) throws CommandException {
        ctxt.em().merge(updateControlledVocabulary);
        if(controlledVocabularyTerms.size() > 0){
            for (ControlledVocabularyTerms term: controlledVocabularyTerms) {
                ctxt.em().merge(term);
            }
        }

        if (deleteControlledVocabulary.size() > 0) {
            for (ControlledVocabularyTerms deleteTerm: deleteControlledVocabulary) {
                ControlledVocabularyTerms doomedAndMerged = ctxt.em().merge(deleteTerm);
                ctxt.em().remove(doomedAndMerged);
            }
        }
    }
}
