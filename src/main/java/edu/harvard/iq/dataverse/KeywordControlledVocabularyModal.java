package edu.harvard.iq.dataverse;

import javax.ejb.EJB;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * @author CIMMYT
 */
@ViewScoped
@Named("KeywordControlledVocabularyModal")
public class KeywordControlledVocabularyModal implements java.io.Serializable {

    private static final Logger logger = Logger.getLogger(KeywordControlledVocabularyModal.class.getCanonicalName());

    @EJB
    DataverseServiceBean dataverseService;

    private Long ownerId;
    private Long datasetId;
    private TreeSet<ControlledVocabulary> controlledVocabularies;
    private TreeSet<ControlledVocabularyTerms> controlledVocabularyTerms;
    private ControlledVocabulary selectedControlledVocabulary;
    private ControlledVocabularyTerms selectedControlledVocabularyTerm;

    private Long selectedControlledVocabularyId;
    private Long selectedControlledVocabularyTermId;

    private String vocabulary;
    private String term;
    private String url;

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public ControlledVocabulary getSelectedControlledVocabulary() {
        return selectedControlledVocabulary;
    }

    public TreeSet<ControlledVocabulary> getControlledVocabularies() {
        return controlledVocabularies;
    }

    public void setControlledVocabularies(TreeSet<ControlledVocabulary> controlledVocabularies) {
        this.controlledVocabularies = controlledVocabularies;
    }

    public TreeSet<ControlledVocabularyTerms> getControlledVocabularyTerms() {
        return controlledVocabularyTerms;
    }

    public void setControlledVocabularyTerms(TreeSet<ControlledVocabularyTerms> controlledVocabularyTerms) {
        this.controlledVocabularyTerms = controlledVocabularyTerms;
    }

    public Long getSelectedControlledVocabularyId() {
        return selectedControlledVocabularyId;
    }

    public void setSelectedControlledVocabularyId(Long selectedControlledVocabularyId) {
        this.selectedControlledVocabularyId = selectedControlledVocabularyId;
    }

    public Long getSelectedControlledVocabularyTermId() {
        return selectedControlledVocabularyTermId;
    }

    public void setSelectedControlledVocabularyTermId(Long selectedControlledVocabularyTermId) {
        this.selectedControlledVocabularyTermId = selectedControlledVocabularyTermId;
    }

    public String getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(String vocabulary) {
        this.vocabulary = vocabulary;
    }

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

    public void loadOwnerControlledVocabulary() {

        if (ownerId == null) {
            return;
        }
        logger.info("RICVAZO: Se llamó a la rutina de crear nueva lista de vocabulario controlados");
        logger.info("El ID del OwnerID: " + ownerId.toString());

        controlledVocabularies = new TreeSet<>((cv1, cv2) -> cv1.getVocabularyName().compareToIgnoreCase(cv2.getVocabularyName()));
        selectedControlledVocabularyId = null;
        selectedControlledVocabulary = null;
        selectedControlledVocabularyTermId = null;
        selectedControlledVocabularyTerm = null;
        vocabulary = null;
        term = null;
        url = null;

        Dataverse dv = dataverseService.find(ownerId);
        controlledVocabularies.addAll(getDataverseControlledVocabularies(dv));
    }

    public void loadDatasetOwnerControlledVocabulary(String SownerId) {
        if (SownerId == null) {
            return;
        }

        logger.info("Se llamá a la rutina para cargar los vocabularios controllados cuando se edita la metadata de un dataset");
        logger.info("El ID del OwnerID: " + SownerId);
        ownerId = Long.parseLong(SownerId);

        controlledVocabularies = new TreeSet<>((cv1, cv2) -> cv1.getVocabularyName().compareToIgnoreCase(cv2.getVocabularyName()));
        selectedControlledVocabularyId = null;
        selectedControlledVocabulary = null;
        selectedControlledVocabularyTermId = null;
        selectedControlledVocabularyTerm = null;
        vocabulary = null;
        term = null;
        url = null;

        Dataverse dv = dataverseService.find(ownerId);
        controlledVocabularies.addAll(getDataverseControlledVocabularies(dv));
    }

    private List<ControlledVocabulary> getDataverseControlledVocabularies(Dataverse dv) {

        if (dv.getControlledvocabularies() != null && !dv.getControlledvocabularies().isEmpty()) {
            return dv.getControlledvocabularies();
        } else {
            if (dv.getOwner() != null) {
                return getDataverseControlledVocabularies((Dataverse) dv.getOwner());
            } else {
                return new ArrayList<>();
            }
        }

    }

    public void controlledVocabularyChanged(AjaxBehaviorEvent e) {
        logger.info("RICVAZO: Se llamó a la rutina de crear nueva lista de vocabulario controlados");
        selectedControlledVocabulary = null;
        selectedControlledVocabularyTermId = null;
        selectedControlledVocabularyTerm = null;
        vocabulary = null;
        term = null;
        url = null;
        for (ControlledVocabulary cv : controlledVocabularies) {
            if (cv.getId().equals(selectedControlledVocabularyId)) {
                selectedControlledVocabulary = cv;
                break;
            }
        }

        controlledVocabularyTerms = new TreeSet<>((cv1, cv2) -> cv1.getTerm().compareToIgnoreCase(cv2.getTerm()));
        if (selectedControlledVocabulary != null) {
            vocabulary = selectedControlledVocabulary.getVocabularyName();
            controlledVocabularyTerms.addAll(selectedControlledVocabulary.getTerms());
        }
    }

    public void controlledVocabularyTermChanged(AjaxBehaviorEvent e) {
        selectedControlledVocabularyTerm = null;
        term = null;
        url = null;

        for (ControlledVocabularyTerms cvt : controlledVocabularyTerms) {
            if (cvt.getId().equals(selectedControlledVocabularyTermId)) {
                selectedControlledVocabularyTerm = cvt;
                break;
            }
        }

        if (selectedControlledVocabularyTerm != null) {
            term = selectedControlledVocabularyTerm.getTerm();
            url = selectedControlledVocabularyTerm.getUrl();
        }
    }

    public void clearSelectedValuesAndLists() {
        logger.info("RICVAZO: Reset remote command called!!");
        selectedControlledVocabulary = null;
        selectedControlledVocabularyId = null;
        selectedControlledVocabularyTerm = null;
        selectedControlledVocabularyTermId = null;
        vocabulary = null;
        term = null;
        url = null;
        controlledVocabularyTerms = new TreeSet<>();
    }

}