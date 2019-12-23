package edu.harvard.iq.dataverse;


import com.opencsv.CSVReader;
import edu.harvard.iq.dataverse.engine.command.Command;
import edu.harvard.iq.dataverse.engine.command.exception.CommandException;
import edu.harvard.iq.dataverse.engine.command.impl.*;
import edu.harvard.iq.dataverse.util.BundleUtil;
import org.apache.tika.parser.txt.CharsetDetector;
import org.primefaces.PrimeFaces;
import org.primefaces.context.PrimeRequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import edu.harvard.iq.dataverse.util.JsfHelper;
import static edu.harvard.iq.dataverse.util.JsfHelper.JH;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author CIMMYT
 */
@SessionScoped
@ManagedBean
public class CreateVocabularyPage implements java.io.Serializable{
    @EJB
    DataverseServiceBean dataverseService;

    @EJB
    EjbDataverseEngine commandEngine;

    @EJB
    VocabularyServiceBean vocabularyService;


    @Inject
    DataverseRequestServiceBean dvRequestService;

    @Inject
    PermissionsWrapper permissionsWrapper;

    @Inject
    DataverseSession session;


    private Dataverse dataverse;
    private Long dataverseId;

    private File tempDir;
    private File uploadedFile;
    private StreamedContent csvFile;

    private List<ControlledVocabularyValue> languagesAvailable;

    private ControlledVocabulary selectControlledvocabulary;
    private List<ControlledVocabularyTerms> selectedcontrolledVocabularyTerms;

    private ControlledVocabulary newVocabulary;
    private List<String> newVocabularyHeaders;
    private List<String[]> newVocabularyTerms;
    private List<ControlledVocabularyTerms> newControlledVocabularyTerms;


    private static final Logger logger = Logger.getLogger(ThemeWidgetFragment.class.getCanonicalName());

    private  void createTempDir() {
        try {
            File tempRoot = Files.createDirectories(Paths.get("../docroot/logos/temp")).toFile();
            tempDir = Files.createTempDirectory(tempRoot.toPath(),getDataverseId().toString()).toFile();
        } catch (IOException e) {
            throw new RuntimeException("Error creating temp directory", e); // improve error handling
        }
    }


    @PreDestroy
    /**
     *  Cleanup by deleting temp directory and uploaded files
     */
    public void cleanupTempDirectory() {
        try {

            if (tempDir != null) {
                for (File f : tempDir.listFiles()) {
                    Files.deleteIfExists(f.toPath());
                }
                Files.deleteIfExists(tempDir.toPath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error deleting temp directory", e); // improve error handling
        }
        uploadedFile=null;
        tempDir=null;
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

    public ControlledVocabulary getNewVocabulary() {
        return newVocabulary;
    }

    public void setNewVocabulary(ControlledVocabulary newVocabulary) {
        this.newVocabulary = newVocabulary;
    }

    public List<String[]> getNewVocabularyTerms() {
        return newVocabularyTerms;
    }

    public List<String> getNewVocabularyHeaders() {
        return newVocabularyHeaders;
    }

    public void setSelectControlledvocabulary(ControlledVocabulary selectControlledvocabulary) {
        this.selectControlledvocabulary = selectControlledvocabulary;
    }

    public ControlledVocabulary getSelectControlledvocabulary() {
        return selectControlledvocabulary;
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

    public List<ControlledVocabularyTerms> getNewControlledVocabularyTerms() {
        return newControlledVocabularyTerms;
    }

    public void setNewControlledVocabularyTerms(List<ControlledVocabularyTerms> newControlledVocabularyTerms) {
        this.newControlledVocabularyTerms = newControlledVocabularyTerms;
    }

    public String init() {
        dataverse = dataverseService.find(dataverseId);
        if (dataverse == null) {
            return permissionsWrapper.notFound();
        }
        if (!permissionsWrapper.canIssueCommand(dataverse, CreateVocabularyCommand.class)) {
            return permissionsWrapper.notAuthorized();
        }

        newVocabulary = new ControlledVocabulary();
        newVocabulary.setCreateTime(new Timestamp(new Date().getTime()));
        newVocabulary.setOwner(dataverse);

        languagesAvailable = vocabularyService.getLanguages();


        FileDownloadView();
        return null;
    }

    public List<String> onCompleteLanguage(String query){
        if (languagesAvailable == null){
            return new ArrayList<String>();
        }
        List<String> consultQuery = new ArrayList<>();
        for (ControlledVocabularyValue value: languagesAvailable) {
            if (value.getStrValue().toLowerCase().startsWith(query.toLowerCase())) {
                consultQuery.add(value.getStrValue());
            }
        }
        return consultQuery;
    }


    public String save() {
        Command<Void> cmd;
        DataverseControlledVocabulary dataverseControlledVocabularies = createDataverseControlledVocabulary();
        //List<ControlledVocabularyTerms> cvTerms = createControlledVocabularyTerms();
        cmd = new CreateVocabularyCommand(dataverseControlledVocabularies, newVocabulary, newControlledVocabularyTerms, dvRequestService.getDataverseRequest(), dataverse);
        try {
            commandEngine.submit(cmd);
        } catch (CommandException e) {
            logger.log(Level.SEVERE, "Error created Vocabulary", e);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, BundleUtil.getStringFromBundle("vocabulary.save.failed"), null));
        }
        this.cleanupTempDirectory();
        newVocabulary = null;
        newControlledVocabularyTerms = null;
        return "manage-vocabularies.xhtml?faces-redirect=true&dataverseId="+dataverse.getId();
    }

    public Boolean VocabularyExist(String vocabularyName){
        List<String> vocabulariesNames = new ArrayList<>();
        for (DataverseControlledVocabulary vocabulary: dataverse.getVocabularies()) {
            vocabulariesNames.add(vocabulary.getControlledVocabulary().getVocabularyName().toLowerCase());
        }
        return vocabulariesNames.contains(vocabularyName.toLowerCase());
    }


    public String cancel() {
        return "manage-vocabularies.xhtml?faces-redirect=true&dataverseId="+dataverse.getId(); // go to dataverse page
    }

    public DataverseControlledVocabulary createDataverseControlledVocabulary(){
        PrimeFaces.current().executeScript("console.log('Estoy en createDataverseControlledVocabulary');");
        DataverseControlledVocabulary newDataverseControlledVocabulary = new DataverseControlledVocabulary(dataverse, newVocabulary);
        return newDataverseControlledVocabulary;
    }

    public List<ControlledVocabularyTerms> createControlledVocabularyTerms(){
        PrimeFaces.current().executeScript("console.log('Estoy en createControlledVocabularyTerms');");
        List<ControlledVocabularyTerms> newTerms = new ArrayList<>();
        int termIndex = newVocabularyHeaders.indexOf("term");
        int urlIndex = newVocabularyHeaders.indexOf("url");
        for (String[] newTerm: newVocabularyTerms) {
            ControlledVocabularyTerms ncvt = new ControlledVocabularyTerms(newVocabulary);
            ncvt.setTerm(newTerm[termIndex]);
            ncvt.setUrl(newTerm[urlIndex]);
            newTerms.add(ncvt);
        }
        return newTerms;
    }


    public void handleFileUpload(FileUploadEvent event) {
        if (newVocabularyTerms != null && newVocabularyHeaders != null) {
            newVocabularyTerms = null;
            newVocabularyHeaders = null;
        }
        logger.finer("entering fileUpload");
        if (this.tempDir==null) {
            createTempDir();
            logger.finer("created tempDir");
        }
        UploadedFile uFile = event.getFile();
        try {
            uploadedFile = new File(tempDir, uFile.getFileName());
            if (!uploadedFile.exists()) {
                uploadedFile.createNewFile();
            }
            logger.finer("created file");
            Files.copy(uFile.getInputstream(), uploadedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.finer("copied inputstream to file");

            String readCSVresponse = readCSV(uploadedFile);
            if (readCSVresponse != null) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, uFile.getFileName(), readCSVresponse);
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, uFile.getFileName(), " Has been upload suceffully");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;

        } catch (IOException e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error uploading csv file: ", e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            logger.finer("caught IOException");
            logger.throwing("ManageVocabulariesPage", "handleFileUpload", e);
            throw new RuntimeException("Error uploading csv file", e); // improve error handling
        }

    }

    public  String readCSV(File ucsvFile) throws IOException {
        String fileName = ucsvFile.getPath();
        String enconde = getEndcoding(ucsvFile);
        try {
            FileInputStream filefis = new FileInputStream(fileName);
            InputStreamReader fileisr = new InputStreamReader(filefis, enconde);
            BufferedReader readfile = new BufferedReader(fileisr);
            CSVReader reader = new CSVReader(readfile);
            String[] headers = reader.readNext();
            if(!ValidateCSVHeaders(headers)){
                return "CSV struct error, the CSV has not a valid field or some field has repeat";
            }
            newVocabularyTerms = validateRownContent(reader.readAll());
            newControlledVocabularyTerms = createControlledVocabularyTerms();
            return null;
        } catch (FileNotFoundException e) {
            e.getMessage();
            return e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }


    public String getEndcoding(File ucsvFile) throws IOException {
        String fileName = ucsvFile.getPath();
        BufferedInputStream  content = new BufferedInputStream(new FileInputStream(fileName));
        CharsetDetector cd = new CharsetDetector();
        cd.setText(content);
        return cd.detect().getName();
    }


    public Boolean ValidateCSVHeaders(String[] csvHeaders){
        boolean validateColumns = true;
        int control = 0;
        for (String csvColumn: csvHeaders) {
            if (csvColumn.equals("url") && CountHeaders(csvHeaders, csvColumn))
                validateColumns = true;
            else  if (csvColumn.equals("term") && CountHeaders(csvHeaders, csvColumn)){
                validateColumns = true;
                control++;
            }
            else{
                logger.log(Level.SEVERE, "Error in CSV structure " + csvColumn + " is not valid field");
                return false;
            }
        }
        if (control != 1) {
            return false;
        }
        newVocabularyHeaders = Arrays.asList(csvHeaders);
        return validateColumns;
    }

    public boolean CountHeaders(String[] csvHeaders, String header){
        int countheader = 0;
        for (String csvheader: csvHeaders) {
            if (header.matches(csvheader)) {
                countheader++;
            }
        }
        return (countheader == 1)? true: false;
    }


    public List<String []> validateRownContent(List<String[]> csvRows){
        List<String []> validRows = new ArrayList<>();
        for (String[] row: csvRows) {
            if ( row[newVocabularyHeaders.indexOf("term")].length() != 0) {
                validRows.add(row);
            }
        }
        return validRows;
    }

    public boolean rowsExists() {
        return newVocabularyTerms!=null;
    }


    public void  FileDownloadView(){
        PrimeFaces.current().executeScript("console.log(' A ver que sale file download');");
        InputStream stream = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/resources/files/example_csv.csv");
        if (stream == null) {
            PrimeFaces.current().executeScript("console.log(' A ver que sale file download 2 -2 ');");
        }
        csvFile = new DefaultStreamedContent(stream, "text/csv", "csv_example.csv");
    }

    public StreamedContent getCsvFile() {
        return this.csvFile;
    }


}
