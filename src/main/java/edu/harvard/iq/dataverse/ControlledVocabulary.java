package edu.harvard.iq.dataverse;

import edu.harvard.iq.dataverse.util.DateUtil;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.*;


/**
 *
 * @author CIMMYT
 */

@Entity
public class ControlledVocabulary implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vocabularyName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVocabularyName() {
        return vocabularyName;
    }

    public void setVocabularyName(String vocabularyName) {
        this.vocabularyName = vocabularyName;
    }

    private String vocabularyLanguage;

    public void setVocabularyLanguage(String vocabularyLanguage) {
        this.vocabularyLanguage = vocabularyLanguage;
    }

    public String getVocabularyLanguage() {
        return vocabularyLanguage;
    }

    @Column(nullable = false)
    private int imports = 0;

    public int getImports() {
        return imports;
    }

    public void setImports(int imports) {
        this.imports = imports;
    }


    @Temporal(value = TemporalType.TIMESTAMP)
    @Column( nullable = false )
    private Date createTime;

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreateDate() {
        return DateUtil.formatDate(createTime);
    }

    @OneToMany(mappedBy = "vocabulary", cascade = {CascadeType.ALL})
    private List<ControlledVocabularyTerms> terms;

    public void setTerms(List<ControlledVocabularyTerms> terms) {
        this.terms = terms;
    }

    public List<ControlledVocabularyTerms> getTerms() {
        return terms;
    }

    @OneToMany(mappedBy = "controlledVocabulary")
    private List<DataverseControlledVocabulary> vocabularies;


    @ManyToOne
    private Dataverse owner;

    public Dataverse getOwner() {
        return owner;
    }

    public void setOwner(Dataverse owner) {
        this.owner = owner;
    }
}
