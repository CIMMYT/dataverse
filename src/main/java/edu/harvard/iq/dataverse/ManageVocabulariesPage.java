package edu.harvard.iq.dataverse;

import edu.harvard.iq.dataverse.engine.command.Command;
import edu.harvard.iq.dataverse.engine.command.exception.CommandException;
import edu.harvard.iq.dataverse.engine.command.impl.CreateVocabularyCommand;
import edu.harvard.iq.dataverse.engine.command.impl.DeleteImportVocabularyCommand;
import edu.harvard.iq.dataverse.engine.command.impl.DeleteVocabularyCommand;
import edu.harvard.iq.dataverse.engine.command.impl.ImportVocabularyCommand;
import edu.harvard.iq.dataverse.util.BundleUtil;

import edu.harvard.iq.dataverse.util.JsfHelper;
import org.primefaces.PrimeFaces;

import static edu.harvard.iq.dataverse.util.JsfHelper.JH;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import javax.faces.context.FacesContext;


/**
 *
 * @author CIMMYT
 */

@ManagedBean
@SessionScoped
public class ManageVocabulariesPage implements java.io.Serializable {

    @EJB
    DataverseServiceBean dvService;

    @EJB
    EjbDataverseEngine commandEngine;

    @EJB
    VocabularyServiceBean vocabularyService;

    @EJB
    EjbDataverseEngine engineService;

    @PersistenceContext(unitName = "VDCNet-ejbPU")
    EntityManager em;

    @Inject
    DataversePage dvpage;

    @Inject
    DataverseSession session;

    @Inject
    DataverseRequestServiceBean dvRequestService;

    @Inject
    PermissionsWrapper permissionsWrapper;


    private static final Logger logger = Logger.getLogger(ThemeWidgetFragment.class.getCanonicalName());


    private Dataverse dataverse;
    private Long dataverseId;
    private List<DataverseControlledVocabulary> dataversecontrolledvocabularies;
    private DataverseControlledVocabulary selecteddataversecontrolledvocaulary ;
    private List<ControlledVocabularyTerms> selectedcontrolledVocabularyTerms;

    private List<ControlledVocabulary> cvforimport;
    private ControlledVocabulary selectControlledvocabulary;


    public String init() {
        dataverse = dvService.find(dataverseId);
        if (dataverse == null) {
            return permissionsWrapper.notFound();
        }
        if (!permissionsWrapper.canIssueCommand(dataverse, CreateVocabularyCommand.class)) {
            return permissionsWrapper.notAuthorized();
        }

        cvforimport = getVocabulariesForImport();

        dataversecontrolledvocabularies = getVocabularies();
        return null;
    }


    public List<DataverseControlledVocabulary> getDataversecontrolledvocabularies() {
        if (dataversecontrolledvocabularies == null) {
            dataversecontrolledvocabularies = getVocabularies();
        }
        return dataversecontrolledvocabularies;
    }

    public List<DataverseControlledVocabulary> getVocabularies(){
        if (dataverse == null) {
            return null;
        }
        List<DataverseControlledVocabulary> vocabularies = new ArrayList<>();
        for (DataverseControlledVocabulary vocabulary: dataverse.getVocabularies()) {
            vocabularies.add(vocabulary);
        }
        return vocabularies;
    }

    public void setDataversecontrolledvocabularies(List<DataverseControlledVocabulary> dataversecontrolledvocabularies) {
        this.dataversecontrolledvocabularies = dataversecontrolledvocabularies;
    }

    public Dataverse getDataverse() {
        return dataverse;
    }

    public void setDataverse(Dataverse dataverse) {
        this.dataverse = dataverse;
    }

    public Long getDataverseId() {
        return dataverseId;
    }

    public void setDataverseId(Long dataverseId) {
        this.dataverseId = dataverseId;
    }


    public DataverseControlledVocabulary getSelecteddataversecontrolledvocaulary() {
        return selecteddataversecontrolledvocaulary;
    }

    public void setSelecteddataversecontrolledvocaulary(DataverseControlledVocabulary selecteddataversecontrolledvocaulary) {
        this.selecteddataversecontrolledvocaulary = selecteddataversecontrolledvocaulary;
    }

    public void setSelectControlledvocabulary(ControlledVocabulary selectControlledvocabulary) {
        this.selectControlledvocabulary = selectControlledvocabulary;
    }

    public ControlledVocabulary getSelectControlledvocabulary() {
        return selectControlledvocabulary;
    }

    public List<ControlledVocabulary> getCvforimport() {
        return cvforimport;
    }

    public void setCvforimport(List<ControlledVocabulary> cvforimport) {
        this.cvforimport = cvforimport;
    }

    public void deleteVocabulary() {
        if (selecteddataversecontrolledvocaulary.getControlledVocabulary().getImports()  > 0){
            String aux = BundleUtil.getStringFromBundle("vocabulary.delete.error.1");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info: ", aux));
            return;
        }
        dataversecontrolledvocabularies.remove(selecteddataversecontrolledvocaulary);
        Command<Void> cmd;
        cmd = new DeleteVocabularyCommand(selecteddataversecontrolledvocaulary, dvRequestService.getDataverseRequest(), getDataverse());
        try {
            engineService.submit(cmd);
            JsfHelper.addFlashMessage(BundleUtil.getStringFromBundle("vocabulary.delete"));
            //FacesContext.getCurrentInstance().addMessage(null, new FacesMessage( FacesMessage.SEVERITY_INFO, "Success: ", BundleUtil.getStringFromBundle("vocabulary.delete")));
        } catch (CommandException e) {
            JsfHelper.addErrorMessage(BundleUtil.getStringFromBundle("vocabulary.delete.error"));
            //FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Fatal: ", BundleUtil.getStringFromBundle("vocabulary.delete.error")));
        }

    }

    public void deleteImportVocabulary() {
        dataversecontrolledvocabularies.remove(selecteddataversecontrolledvocaulary);
        Command<Void> cmd;
        cmd = new DeleteImportVocabularyCommand(selecteddataversecontrolledvocaulary, dvRequestService.getDataverseRequest(), getDataverse());
        try {
            engineService.submit(cmd);
            //FacesContext.getCurrentInstance().addMessage(null, new FacesMessage( FacesMessage.SEVERITY_INFO, "Success: ", BundleUtil.getStringFromBundle("vocabulary.delete")));
            JsfHelper.addFlashMessage(BundleUtil.getStringFromBundle("vocabulary.delete"));
        } catch (CommandException e) {
            JsfHelper.addErrorMessage(BundleUtil.getStringFromBundle("vocabulary.delete.error"));
            //FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "Fatal: ", BundleUtil.getStringFromBundle("vocabulary.delete.error")));
        }
    }

    public void setSelectedcontrolledVocabularyTerms(List<ControlledVocabularyTerms> selectedcontrolledVocabularyTerms) {
        this.selectedcontrolledVocabularyTerms = selectedcontrolledVocabularyTerms;
    }


    public List<ControlledVocabularyTerms> getSelectedcontrolledVocabularyTerms() {
        if (selectedcontrolledVocabularyTerms == null) {
            return new ArrayList<ControlledVocabularyTerms>();
        }
        return selectedcontrolledVocabularyTerms;
    }

    public void getTerms(DataverseControlledVocabulary dvcv){
        List<ControlledVocabularyTerms> terms = new ArrayList<>();
        for (ControlledVocabularyTerms term: dvcv.getControlledVocabulary().getTerms()) {
            terms.add(term);
        }
        selectedcontrolledVocabularyTerms =  terms;
    }

    public void getTermsControlledVocavulary(ControlledVocabulary selectcv){
        PrimeFaces.current().executeScript("console.log(' A ver que sale getTerms');");
        List<ControlledVocabularyTerms> terms = new ArrayList<>();
        for (ControlledVocabularyTerms term: selectcv.getTerms()) {
            terms.add(term);
        }
        selectedcontrolledVocabularyTerms = terms;
    }

    public String redirectControlledVocabularyEdit(DataverseControlledVocabulary dvcv){
        if (dvcv.getIsimport() == true) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage( FacesMessage.SEVERITY_ERROR, "Error: ", "Cannot edit this Vocabulary. It's a import the vocabulary"));
        }
        return "edit-vocabulary.xhtml?faces-redirect=true&vocabularyId="+dvcv.getControlledVocabulary().getId()+"&dataverseId="+dataverse.getId();
    }

    public String save() {
        return "dataverse.xhtml?faces-redirect=true&alias="+dataverse.getAlias();  // go to dataverse page
    }

    public String cancel() {
        return "dataverse.xhtml?faces-redirect=true&alias="+dataverse.getAlias();  // go to dataverse page
    }

    // Import Vocabularies functions

    public List<ControlledVocabulary> getVocabulariesForImport(){
        List<ControlledVocabulary> vocabulariesforimport = new ArrayList<>();
        for (ControlledVocabulary vocabulary: dataverse.getParentVocabularies()) {
            if (!VocabularyExist(vocabulary.getVocabularyName())) {
                vocabulariesforimport.add(vocabulary);
            }
        }
        return vocabulariesforimport;
    }

    public Boolean VocabularyExist(String vocabularyName){
        List<String> vocabulariesNames = new ArrayList<>();

        for (DataverseControlledVocabulary vocabulary: dataverse.getVocabularies()) {
            vocabulariesNames.add(vocabulary.getControlledVocabulary().getVocabularyName().toLowerCase());
        }
        return vocabulariesNames.contains(vocabularyName.toLowerCase());
    }

    public String importvocabulary(){
        Command<Void> cmd;
        DataverseControlledVocabulary importedddvcv = new DataverseControlledVocabulary(dataverse, selectControlledvocabulary);
        dataversecontrolledvocabularies.add(importedddvcv);
        cmd = new ImportVocabularyCommand(importedddvcv,dvRequestService.getDataverseRequest(), dataverse);
        try {
            commandEngine.submit(cmd);
        } catch (CommandException e) {
            logger.log(Level.SEVERE, "Error cannot import this vocabulary", e);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, BundleUtil.getStringFromBundle("dataverse.save.failed"), BundleUtil.getStringFromBundle("dataverse.theme.failure")));
        }
        return "manage-vocabularies.xhtml?faces-redirect=true&dataverseId="+dataverse.getId();
    }


}
