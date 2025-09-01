package ma.digitalia.generationfichepaie.helpers;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import ma.digitalia.generationfichepaie.entities.ElementPaie;
import ma.digitalia.generationfichepaie.entities.FichePaie;
import ma.digitalia.generationfichepaie.Enum.TypeElement;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class FichePaiePdfGenerateur {

    // --- Définition des polices ---
    private static final Font FONT_TITRE = new Font(Font.HELVETICA, 16, Font.BOLD);
    private static final Font FONT_HEADER_TABLE = new Font(Font.HELVETICA, 8, Font.BOLD);
    private static final Font FONT_NORMAL = new Font(Font.HELVETICA, 8, Font.NORMAL);
    private static final Font FONT_BOLD = new Font(Font.HELVETICA, 8, Font.BOLD);
    private static final Font FONT_RED = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.RED);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // --- Ordre d'affichage des éléments ---
    private static final Map<TypeElement, Integer> ELEMENT_TYPE_ORDER = Map.ofEntries(
            Map.entry(TypeElement.SALAIRE_BASE, 10),
            Map.entry(TypeElement.HEURES_SUPPLEMENTAIRES, 20),
            Map.entry(TypeElement.PRIME_FIXE, 30),
            Map.entry(TypeElement.PRIME_VARIABLE, 40),
            Map.entry(TypeElement.INDEMNITE, 50),
            Map.entry(TypeElement.COTISATION_SOCIALE, 100),
            Map.entry(TypeElement.DEDUCTION_ABSENCE, 110),
            Map.entry(TypeElement.DEDUCTION_AUTRE, 120),
            Map.entry(TypeElement.IMPOT, 130)
    );

    private static final Comparator<ElementPaie> ELEMENT_COMPARATOR =
            Comparator.comparing(element -> ELEMENT_TYPE_ORDER.getOrDefault(element.getType(), 999));

    public static byte[] genererPdf(FichePaie fiche) {
        if (fiche == null || fiche.getEmploye() == null || fiche.getElements() == null) {
            throw new IllegalArgumentException("L'objet FichePaie ou ses composants essentiels sont nuls.");
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 25, 25, 25, 25);
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(creerTableEnTete(fiche));
            document.add(new Paragraph(" "));
            document.add(creerTableauElementsPaie(fiche));
            document.add(new Paragraph(" "));
            document.add(creerTableauRecapitulatif(fiche));

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    private static PdfPTable creerTableauElementsPaie(FichePaie fiche) throws DocumentException {
        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{0.5f, 3.5f, 1f, 1.5f, 1f, 1.5f, 1.5f, 1f, 1.5f});
        creerEnTetesTableauPrincipal(table);

        List<ElementPaie> elementsTries = fiche.getElements();
        elementsTries.sort(ELEMENT_COMPARATOR);

        boolean aInsereTotauxBruts = false;
        boolean aInsereTotalCotisations = false; // Flag pour s'assurer qu'on n'insère le total qu'une seule fois

        for (ElementPaie element : elementsTries) {

            // --- Injecter les totaux bruts ---
            if (!isGainType(element.getType()) && !aInsereTotauxBruts) {
                ajouterLigneTotale(table, "Total Brut", formatBigDecimal(fiche.getSalaireBrut()), 5);
                ajouterLigneTotale(table, "Total Brut imposable", formatBigDecimal(fiche.getSalaireBrutImposable()), 5);
                ajouterLigneSeparation(table);
                aInsereTotauxBruts = true;
            }

            // --- NOUVELLE LOGIQUE : Injecter le total des cotisations ---
            // S'exécute quand on rencontre le premier élément qui n'est NI un gain, NI une cotisation.
            if (!isGainType(element.getType()) && !isCotisationType(element.getType()) && !aInsereTotalCotisations) {
                if (fiche.getCotisationsSalariales() != null && fiche.getCotisationsSalariales().compareTo(BigDecimal.ZERO) != 0) {
                    BigDecimal totalPatronal = BigDecimal.ZERO; // TODO: Implémenter le calcul des charges patronales
                    ajouterLigneTotaleCotisations(table, "Total Cotisations", formatBigDecimal(fiche.getCotisationsSalariales()), formatBigDecimal(totalPatronal));
                }
                aInsereTotalCotisations = true; // On marque comme inséré pour ne pas le refaire
            }

            // Affiche la ligne en fonction de son type
            switch (element.getType()) {
                case SALAIRE_BASE:
                case HEURES_SUPPLEMENTAIRES:
                case PRIME_FIXE:
                case PRIME_VARIABLE:
                case INDEMNITE:
                    String nombre = element.getType() == TypeElement.SALAIRE_BASE ? "26" : "";
                    ajouterLigneGain(table, element.getLibelle(), formatBigDecimal(element.getBase()), nombre, formatBigDecimal(element.getMontant()));
                    break;
                case COTISATION_SOCIALE:
                    ajouterLigneCotisation(table, element.getLibelle(), formatBigDecimal(element.getBase()), formatTaux(element.getTaux()), formatBigDecimal(element.getMontant()));
                    break;
                case DEDUCTION_ABSENCE:
                case DEDUCTION_AUTRE:
                case IMPOT:
                    ajouterLigneRetenue(table, element.getLibelle(), formatBigDecimal(element.getMontant()));
                    break;
                default:
                    break;
            }
        }

        // --- GESTION DES CAS DE FIN DE LISTE ---

        // Cas 1 : Si la fiche ne contient QUE des gains
        if (!aInsereTotauxBruts && !elementsTries.isEmpty()) {
            ajouterLigneTotale(table, "Total Brut", formatBigDecimal(fiche.getSalaireBrut()), 5);
            ajouterLigneTotale(table, "Total Brut imposable", formatBigDecimal(fiche.getSalaireBrutImposable()), 5);
        }

        // Cas 2 : Si la fiche se termine par des cotisations (et donc le total n'a pas encore été inséré)
        if (!aInsereTotalCotisations && fiche.getCotisationsSalariales() != null && fiche.getCotisationsSalariales().compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal totalPatronal = BigDecimal.ZERO; // TODO: Implémenter le calcul
            ajouterLigneTotaleCotisations(table, "Total Cotisations", formatBigDecimal(fiche.getCotisationsSalariales()), formatBigDecimal(totalPatronal));
        }

        return table;
    }

    private static boolean isGainType(TypeElement type) {
        return type == TypeElement.SALAIRE_BASE || type == TypeElement.HEURES_SUPPLEMENTAIRES ||
                type == TypeElement.PRIME_FIXE || type == TypeElement.PRIME_VARIABLE || type == TypeElement.INDEMNITE;
    }

    // Méthode utilitaire ajoutée pour identifier les cotisations
    private static boolean isCotisationType(TypeElement type) {
        return type == TypeElement.COTISATION_SOCIALE;
    }

    // ==================================================================================
    // --- LES AUTRES MÉTHODES RESTENT INCHANGÉES ---
    // ==================================================================================

    private static PdfPTable creerTableEnTete(FichePaie fiche) throws DocumentException {
        PdfPTable layoutTable = new PdfPTable(2);
        layoutTable.setWidthPercentage(100);
        layoutTable.setWidths(new float[]{1f, 1f});

        PdfPCell societeInfoCell = new PdfPCell();
        societeInfoCell.setBorder(Rectangle.BOX);
        societeInfoCell.setPadding(10);
        Paragraph pSociete = new Paragraph();
        pSociete.add(new Chunk("STE : ........................\n", FONT_NORMAL));
        pSociete.add(new Chunk("Tél : ........................\n\n", FONT_NORMAL));
        pSociete.add(new Chunk("CNSS N° : ........................\n\n", FONT_NORMAL));

        pSociete.add(new Chunk("N° CIN: "+ fiche.getEmploye().getCin() +"\n\n", FONT_NORMAL));
        pSociete.add(new Chunk("N° CNSS: 55252525\n\n", FONT_NORMAL));
        pSociete.add(new Chunk("Date Embauche : "+ fiche.getEmploye().getDateEmbauche() +"\n\n", FONT_NORMAL));
        pSociete.add(new Chunk("Emploi occupé : "+ fiche.getEmploye().getPoste() +"\n", FONT_NORMAL));
        societeInfoCell.addElement(pSociete);
        layoutTable.addCell(societeInfoCell);

        PdfPCell droiteCell = new PdfPCell();
        droiteCell.setBorder(Rectangle.NO_BORDER);
        droiteCell.addElement(creerTableTitre());
        droiteCell.addElement(new Paragraph(" "));
        droiteCell.addElement(creerTableInfosEmploye(fiche));
        layoutTable.addCell(droiteCell);
        return layoutTable;
    }

    private static PdfPTable creerTableTitre() {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(80);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        PdfPCell cellTitre = creerCellule("BULLETIN DE PAIE", FONT_TITRE, Element.ALIGN_CENTER);
        cellTitre.setBorder(Rectangle.BOX);
        cellTitre.setPadding(8);
        table.addCell(cellTitre);
        return table;
    }

    private static PdfPTable creerTableInfosEmploye(FichePaie fiche) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOX);
        cell.setPadding(8);
        Paragraph p = new Paragraph();
        p.add(new Chunk("Paiement le : __________ Par: virement Ou cheque\n\n", FONT_NORMAL));
        p.add(new Chunk("Matricule : " + fiche.getEmploye().getNumeroEmploye() , FONT_NORMAL));
        p.add(new Chunk("*\n", FONT_RED));
        String prenom = fiche.getEmploye().getPreNom() != null ? fiche.getEmploye().getPreNom() : "";
        p.add(new Chunk("Nom & Prénom : ", FONT_NORMAL));
        p.add(new Chunk(fiche.getEmploye().getNom() + " " + prenom + "\n", FONT_BOLD));
        p.add(new Chunk("ADRESSE : "+ fiche.getEmploye().getAdresse() +"\n\n", FONT_NORMAL));
        p.add(new Chunk("VILLE : tetouan", FONT_NORMAL));
        cell.addElement(p);
        table.addCell(cell);
        return table;
    }

    private static PdfPTable creerTableauRecapitulatif(FichePaie fiche) throws DocumentException {
        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1.2f, 1.5f, 1.8f});

        table.addCell(creerCelluleHeader("Cumul", FONT_HEADER_TABLE));
        table.addCell(creerCelluleHeader("Salaire Brut", FONT_HEADER_TABLE));
        table.addCell(creerCelluleHeader("Charges salariales", FONT_HEADER_TABLE));
        table.addCell(creerCelluleHeader("Charges Patronale", FONT_HEADER_TABLE));
        table.addCell(creerCelluleHeader("Avantages en nature", FONT_HEADER_TABLE));
        table.addCell(creerCelluleHeader("Net imposable", FONT_HEADER_TABLE));
        table.addCell(creerCelluleHeader("jours travaillés", FONT_HEADER_TABLE));
        table.addCell(creerCelluleHeader("Heures supplém.", FONT_HEADER_TABLE));
        table.addCell(creerCelluleHeader("NET A PAYER", FONT_HEADER_TABLE));

        // TODO: Vous devez ajouter les méthodes getChargesPatronales(), getAvantagesEnNature(), getJoursTravailles() à votre classe FichePaie
        BigDecimal chargesPatronales = BigDecimal.ZERO;
        BigDecimal avantagesNature = BigDecimal.ZERO; // Remplacez par fiche.getAvantagesEnNature()
        int joursTravailles = 26; // Remplacez par fiche.getJoursTravailles()

        table.addCell(creerCelluleDonnee("Période " + fiche.getPeriode().toString(), Element.ALIGN_LEFT));
        table.addCell(creerCelluleDonnee(formatBigDecimal(fiche.getSalaireBrut()), Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee(formatBigDecimal(fiche.getCotisationsSalariales()), Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee(formatBigDecimal(chargesPatronales), Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee(formatBigDecimal(avantagesNature), Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee(formatBigDecimal(fiche.getSalaireNetImposable()), Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee(String.valueOf(joursTravailles), Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee("", Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee(formatBigDecimal(fiche.getSalaireNet()), Element.ALIGN_RIGHT, FONT_BOLD));

        return table;
    }

    private static void creerEnTetesTableauPrincipal(PdfPTable table) {
        table.addCell(creerCelluleHeader("N°", FONT_HEADER_TABLE, 1, 2));
        table.addCell(creerCelluleHeader("Désignation", FONT_HEADER_TABLE, 1, 2));
        table.addCell(creerCelluleHeader("Nombre", FONT_HEADER_TABLE, 1, 2));
        table.addCell(creerCelluleHeader("Base", FONT_HEADER_TABLE, 1, 2));
        table.addCell(creerCelluleHeader("Part Salariale", FONT_HEADER_TABLE, 3, 1));
        table.addCell(creerCelluleHeader("Part Patronale", FONT_HEADER_TABLE, 2, 1));
        table.addCell(creerCelluleHeader("Taux", FONT_HEADER_TABLE));
        table.addCell(creerCelluleHeader("Gain", FONT_HEADER_TABLE));
        table.addCell(creerCelluleHeader("Retenue", FONT_HEADER_TABLE));
        table.addCell(creerCelluleHeader("Taux", FONT_HEADER_TABLE));
        table.addCell(creerCelluleHeader("Retenue", FONT_HEADER_TABLE));
    }

    private static void ajouterLigneGain(PdfPTable table, String designation, String base, String nombre, String gain) {
        table.addCell(creerCelluleDonnee("", Element.ALIGN_CENTER));
        table.addCell(creerCelluleDonnee(designation, Element.ALIGN_LEFT));
        table.addCell(creerCelluleDonnee(nombre, Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee(base, Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee("", Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee(gain, Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee("", Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee("", Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee("", Element.ALIGN_RIGHT));
    }

    private static void ajouterLigneCotisation(PdfPTable table, String designation, String base, String tauxSalarial, String retenueSalariale) {
        table.addCell(creerCelluleDonnee("", Element.ALIGN_CENTER));
        table.addCell(creerCelluleDonnee(designation, Element.ALIGN_LEFT));
        table.addCell(creerCelluleDonnee("", Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee(base, Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee(tauxSalarial, Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee("", Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee(retenueSalariale, Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee("", Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee("", Element.ALIGN_RIGHT));
    }

    private static void ajouterLigneRetenue(PdfPTable table, String designation, String retenue) {
        table.addCell(creerCelluleDonnee("", Element.ALIGN_CENTER));
        table.addCell(creerCelluleDonnee(designation, Element.ALIGN_LEFT));
        table.addCell(creerCelluleDonnee("", Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee("", Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee("", Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee("", Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee(retenue, Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee("", Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee("", Element.ALIGN_RIGHT));
    }

    private static void ajouterLigneTotale(PdfPTable table, String libelle, String montant, int colMontant) {
        table.addCell(creerCelluleDonnee("", Element.ALIGN_CENTER));
        PdfPCell cellLibelle = creerCelluleDonnee(libelle, Element.ALIGN_LEFT, FONT_BOLD);
        cellLibelle.setColspan(3);
        table.addCell(cellLibelle);
        for (int i = 4; i < 9; i++) {
            if (i == colMontant) {
                PdfPCell cellMontant = creerCelluleDonnee(montant, Element.ALIGN_LEFT, FONT_BOLD);
                cellMontant.setColspan(2);
                table.addCell(cellMontant);
                i++;
            } else {
                table.addCell(creerCelluleDonnee("", Element.ALIGN_LEFT));
            }
        }
    }

    private static void ajouterLigneTotaleCotisations(PdfPTable table, String libelle, String montantSalarial, String montantPatronal) {
        table.addCell(creerCelluleDonnee("", Element.ALIGN_CENTER));
        PdfPCell cellLibelle = creerCelluleDonnee(libelle, Element.ALIGN_LEFT, FONT_BOLD);
        cellLibelle.setColspan(5);
        table.addCell(cellLibelle);
        table.addCell(creerCelluleDonnee(montantSalarial, Element.ALIGN_RIGHT, FONT_BOLD));
        table.addCell(creerCelluleDonnee("", Element.ALIGN_RIGHT));
        table.addCell(creerCelluleDonnee(montantPatronal, Element.ALIGN_RIGHT, FONT_BOLD));
    }

    private static void ajouterLigneSeparation(PdfPTable table) {
        // Ajoute une ligne vide avec seulement des bordures gauche et droite pour l'esthétique
        for (int i = 0; i < 9; i++) {
            PdfPCell emptyCell = new PdfPCell(new Phrase(" "));
            emptyCell.setBorder(Rectangle.LEFT | Rectangle.RIGHT);
            table.addCell(emptyCell);
        }
    }

    private static PdfPCell creerCellule(String content, Font font, int horizontalAlignment) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setHorizontalAlignment(horizontalAlignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private static PdfPCell creerCelluleHeader(String content, Font font) {
        PdfPCell cell = creerCellule(content, font, Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.BOX);
        return cell;
    }

    private static PdfPCell creerCelluleHeader(String content, Font font, int colspan, int rowspan) {
        PdfPCell cell = creerCelluleHeader(content, font);
        cell.setColspan(colspan);
        cell.setRowspan(rowspan);
        return cell;
    }

    private static PdfPCell creerCelluleDonnee(String content, int horizontalAlignment) {
        return creerCelluleDonnee(content, horizontalAlignment, FONT_NORMAL);
    }

    private static PdfPCell creerCelluleDonnee(String content, int horizontalAlignment, Font font) {
        PdfPCell cell = creerCellule(content, font, horizontalAlignment);
        cell.setBorder(Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM | Rectangle.TOP);
        return cell;
    }

    private static String formatBigDecimal(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) == 0) {
            return "";
        }
        return String.format("%,.2f", value);
    }

    private static String formatTaux(BigDecimal taux) {
        if (taux == null || taux.compareTo(BigDecimal.ZERO) == 0) {
            return "";
        }
        return formatBigDecimal(taux) + "%";
    }
}