
package edu.harvard.iq.dataverse.engine.command.impl;

import edu.harvard.iq.dataverse.*;
import edu.harvard.iq.dataverse.authorization.Permission;
import edu.harvard.iq.dataverse.engine.command.*;
import edu.harvard.iq.dataverse.engine.command.exception.CommandException;

import java.util.List;
/**
 *
 * @author CIMMYT
 */
@RequiredPermissions( Permission.EditDataverse )
public class CreateVocabularyCommand extends AbstractVoidCommand {
    private final DataverseControlledVocabulary dataverseControlledVocabulary;
    private final ControlledVocabulary controlledVocabulary;
    private final List<ControlledVocabularyTerms> controlledVocabularyTerms;
    private final Dataverse dv;

    public CreateVocabularyCommand(DataverseControlledVocabulary  dvControlledVocabulary, ControlledVocabulary cv,
                                   List<ControlledVocabularyTerms> cvt, DataverseRequest aRequest, Dataverse anAffectedDataverse) {
        super(aRequest, anAffectedDataverse);
        dataverseControlledVocabulary = dvControlledVocabulary;
        controlledVocabulary = cv;
        controlledVocabularyTerms = cvt;
        dv = anAffectedDataverse;
    }

    @Override
    public void executeImpl(CommandContext ctxt) throws CommandException {
        ctxt.vocabularies().saveDataverseControlledVocabulary(dataverseControlledVocabulary);
        ctxt.vocabularies().saveControlledVocabulary(controlledVocabulary );
        for (ControlledVocabularyTerms cvt: controlledVocabularyTerms) {
            ctxt.vocabularies().saveControlledVocabularyTerms(cvt);
        }

    }

}
