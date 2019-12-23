package edu.harvard.iq.dataverse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.*;


/**
 *
 * @author CIMMYT
 */

@Entity
public class ControlledVocabularyTerms implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String term;
    private String url;


    public ControlledVocabularyTerms(){

    }

    public ControlledVocabularyTerms(ControlledVocabulary cv){
        this.vocabulary = cv;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    public Long getId() {
        return id;
    }

    public String getTerm() {
        return term;
    }

    public String getUrl() {
        return url;
    }

    @ManyToOne
    @JoinColumn(name = "controlledvocabulary_id")
    private ControlledVocabulary vocabulary;

    public ControlledVocabulary getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(ControlledVocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }

    /*
    @OneToMany(mappedBy="principalTerm", cascade = {CascadeType.ALL})
    private List<ControlledVocabularyTermsAlternative> translates;

    public List<ControlledVocabularyTermsAlternative> getTranslates() {
        return translates;
    }

    public void setTranslates(List<ControlledVocabularyTermsAlternative> translates) {
        this.translates = translates;
    }
    */
}
