package edu.harvard.iq.dataverse;

import edu.harvard.iq.dataverse.engine.command.Command;
import edu.harvard.iq.dataverse.engine.command.exception.CommandException;
import edu.harvard.iq.dataverse.engine.command.impl.EditVocabularyCommand;
import edu.harvard.iq.dataverse.util.BundleUtil;

import edu.harvard.iq.dataverse.util.JsfHelper;
import static edu.harvard.iq.dataverse.util.JsfHelper.JH;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author skraffmiller
 */

@ManagedBean
@SessionScoped
public class EditVocabulary implements java.io.Serializable{

    @EJB
    DataverseServiceBean dataverseService;

    @EJB
    EjbDataverseEngine commandEngine;

    @PersistenceContext(unitName = "VDCNet-ejbPU")
    EntityManager em;


    @Inject
    DataverseRequestServiceBean dvRequestService;

    @Inject
    PermissionsWrapper permissionsWrapper;

    @Inject
    DataverseSession session;


    private Dataverse dataverse;
    private ControlledVocabulary vocabulary;

    private Long dataverseId;
    private Long vocabularyId;

    private List<ControlledVocabularyTerms> cvTerms;
    private List<ControlledVocabularyTerms> cvTermsForDelete;


    private List<NewTerm> newTerms = new ArrayList<>();
    private NewTerm newTerm;


    private static final Logger logger = Logger.getLogger(EditVocabulary.class.getCanonicalName());

    public String init() {

        dataverse = dataverseService.find(dataverseId);
        if (dataverse == null) {
            return permissionsWrapper.notFound();
        }

        vocabulary = em.find(ControlledVocabulary.class, vocabularyId);
        if (vocabulary== null) {
            return permissionsWrapper.notFound();
        }

        if(vocabulary.getOwner().getId() != dataverse.getId())
            return permissionsWrapper.notFound();

        cvTerms = new ArrayList<>();
        for (ControlledVocabularyTerms term: vocabulary.getTerms()) {
            cvTerms.add(term);
        }

        cvTermsForDelete = new ArrayList<>();

        newTerm = new NewTerm();

        return null;
    }

    public Long getDataverseId() {
        return dataverseId;
    }

    public void setDataverseId(Long dataverseId) {
        this.dataverseId = dataverseId;
    }

    public void setVocabularyId(Long vocabularyId) {
        this.vocabularyId = vocabularyId;
    }

    public Long getVocabularyId() {
        return vocabularyId;
    }

    public Dataverse getDataverse() {
        return dataverse;
    }

    public void setDataverse(Dataverse dataverse) {
        this.dataverse = dataverse;
    }

    public ControlledVocabulary getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(ControlledVocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }

    public List<ControlledVocabularyTerms> getCvTerms() {
        return cvTerms;
    }

    public void setCvTerms(List<ControlledVocabularyTerms> cvTerms) {
        this.cvTerms = cvTerms;
    }

    public List<ControlledVocabularyTerms> getCvTermsForDelete() {
        return cvTermsForDelete;
    }

    public void setCvTermsForDelete(List<ControlledVocabularyTerms> cvTermsForDelete) {
        this.cvTermsForDelete = cvTermsForDelete;
    }

    public List<NewTerm> getNewTerms() {
        return newTerms;
    }

    public void setNewTerms(List<NewTerm> newTerms) {
        this.newTerms = newTerms;
    }

    public NewTerm getNewTerm() {
        return newTerm;
    }

    public void setNewTerm(NewTerm newTerm) {
        this.newTerm = newTerm;
    }

    public void reinit(){
        newTerm = new NewTerm();
    }

    public void deleteTerm(ControlledVocabularyTerms term){
        cvTerms.remove(term);
        cvTermsForDelete.add(term);
    }

    public void restoreTerm(ControlledVocabularyTerms term){
        cvTermsForDelete.remove(term);
        cvTerms.add(term);
    }


    public String Save(){
        Command<Void> cmd;

        if (newTerms.size() > 0) {
            for (NewTerm nTerm: newTerms) {
                ControlledVocabularyTerms term = new ControlledVocabularyTerms();
                term.setVocabulary(vocabulary);
                term.setTerm(nTerm.getTerm());
                term.setUrl(nTerm.getUrl());
                cvTerms.add(term);
            }
        }

        cmd = new EditVocabularyCommand(vocabulary, cvTerms, cvTermsForDelete, dvRequestService.getDataverseRequest(), dataverse);

        try{
            commandEngine.submit(cmd);
        }catch (CommandException e){
            logger.log(Level.SEVERE, BundleUtil.getStringFromBundle("vocabulary.editform.failed"), e);
            return "edit-vocabulary.xhtml?faces-redirect=true&vocabularyId="+vocabulary.getId()+"&dataverseId="+dataverse.getId();
        }
        JsfHelper.addFlashMessage(BundleUtil.getStringFromBundle("vocabulary.editform.succes"));
        return "edit-vocabulary.xhtml?faces-redirect=true&vocabularyId="+vocabulary.getId()+"&dataverseId="+dataverse.getId();
    }

    public String Cancel(){
        return "manage-vocabularies.xhtml?dataverseId="+dataverse.getId();
    }

    public class NewTerm{

        private String term;
        private String url;

        public String getTerm() {
            return term;
        }

        public void setTerm(String term) {
            this.term = term;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

}
