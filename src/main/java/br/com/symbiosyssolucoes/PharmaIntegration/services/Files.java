package br.com.symbiosyssolucoes.PharmaIntegration.services;


import br.com.symbiosyssolucoes.PharmaIntegration.entity.Invoice;
import br.com.symbiosyssolucoes.PharmaIntegration.entity.InvoiceItem;

import lombok.Data;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.BufferedReader;
import java.io.File;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Data

@Service
public class Files {




    public void menu(Scanner scanner) throws IOException {
        Boolean isTrue = true;

        while (isTrue) {
            System.out.println("Qual ação você quer executar?");
            System.out.println("0 - Voltar ao menu anterior");
            System.out.println("1 - Listar arquivos");
            System.out.println("2 - Mover Arquivo");
            System.out.println("3 - Ler Arquivo");
            System.out.println("4 - Teste EMS");
            System.out.println("5 - Teste Consys");

            int opcao = scanner.nextInt();

            switch (opcao) {
                case 1:
                    this.listFile("C:\\Symbiosys\\ped\\");
                    break;
                case 2:
                    this.moveFile("C:\\Symbiosys\\ped\\teste.txt","C:\\Symbiosys\\imp\\teste.txt");
                    break;
                case 3:
                    this.readFile("C:\\Symbiosys\\ped\\1.txt");
                    break;
                case 4:
                    this.toEms("C:\\Symbiosys\\ped\\1.txt");
                    break;
                case 5:
                    this.toConsys("C:\\Symbiosys\\ped\\2.PED");
                    break;
                default:
                    isTrue = false;
                    break;

            }
        }
        System.out.println();
    }


    public String[] listFile(String path) {


        File folder = new File(path);

        String[] files = folder.list();

        for(int i = 0; i < files.length; i++ ){
            System.out.println(files[i]);
        }




        return files;
    }

    public void moveFile(String srcFile, String destPath) {

        File source = new File(srcFile);
        File destination = new File(destPath);
        source.renameTo(destination);


    }

    public String mkDir(String dirName) {

        File folder = new File(dirName);
        if (folder.exists() != true){
            folder.mkdir();
        }
        return  folder.getAbsolutePath();
    }

    public void readFile(String srcFile) {

        File file = new File(srcFile);
        if(file.exists() != true) {
            System.out.println("Nao existe o arquivo solicitado");

        } else {

            try{
                FileReader fileReader = new FileReader(file);
                String line = "";
                BufferedReader bufferedReader = new BufferedReader(fileReader);

                while ( (line = bufferedReader.readLine()) != null) {
                    System.out.println(line);
                }

                fileReader.close();
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public Invoice toEms(String filePath){
        File file = new File(filePath);
        Invoice pedido = new Invoice();

        List<InvoiceItem> items = new ArrayList<InvoiceItem>();
        if(file.exists() != true) {
            System.out.println("Nao existe o arquivo solicitado");

        } else {

            try{
                FileReader fileReader = new FileReader(file);

                String line = "";
                BufferedReader bufferedReader = new BufferedReader(fileReader);

                while ( (line = bufferedReader.readLine()) != null) {
                    if(Integer.parseInt(line.substring(0,1)) == 1){
                      try {

                          pedido.setCustomerCNPJ(line.substring(1,15));
                          pedido.setNumber(line.substring(16,28).trim());
                          int year = Integer.parseInt(line.substring(32,36));
                          int month = Integer.parseInt(line.substring(30,32));
                          int day = Integer.parseInt(line.substring(28,30));
                          LocalDate invoiceDate = LocalDate.of(year,month,day);
                          pedido.setDate(invoiceDate);
                          pedido.setCustomerInvoiceNumber(line.substring(43,58).trim());
                          pedido.setAgentCode(line.substring(58,62).replace("/\r/", ""));

                          pedido.setOperationDate(LocalDateTime.now());
                          pedido.setStatus("1");

                      } catch (IndexOutOfBoundsException i) {
                          i.printStackTrace();
                      }


                    }
                    if(Integer.parseInt(line.substring(0,1)) == 2){
                        try {

                            InvoiceItem item = new InvoiceItem();

                            item.setItemCode(line.substring(13,26));
                            item.setQuantity(Double.parseDouble(line.substring(26,31)));
                            item.setDiscount(Double.parseDouble(line.substring(31,34) + "." + line.substring(34,36)));
                            item.setPaymentTerms(line.substring(36,39));
                            item.setDiscountType(line.substring(39,40));
                            item.setInstallmentType(line.substring(40,41));

                            item.setItemStatus("1");


                            items.add(item);



                        } catch (IndexOutOfBoundsException i) {
                            i.printStackTrace();
                        } catch (NullPointerException n) {
                            System.out.println("Variavel Nula");
                        }



                    }

                }

                pedido.setItems(items);


                fileReader.close();
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return pedido;
    }

    public List<Invoice> toConsys(String filePath){
        String separador = "\n(?=1)";
        File file = new File(filePath);

        List<Invoice> pedidos = new ArrayList<Invoice>();


        if(file.exists() != true) {
            System.out.println("Nao existe o arquivo solicitado");


        } else {

            try{
                String wholeFile = "";
                List<String> multipleInvoices = new ArrayList<String>();

                FileReader fileReader = new FileReader(file);
                String line = "";
                BufferedReader bufferedReader = new BufferedReader(fileReader);

                while ( (line = bufferedReader.readLine()) != null) {

                    wholeFile = wholeFile + line + "\n";
                }
                fileReader.close();
                bufferedReader.close();

                for(int i = 0; i < wholeFile.split(separador).length; i++){
                    multipleInvoices.add(wholeFile.split(separador)[i]);
                }

                multipleInvoices.forEach(ped -> {
                    List<String> pedido = Arrays.asList(ped.split("\n"));
                    Invoice invoice = new Invoice();
                    List<InvoiceItem> items = new ArrayList<InvoiceItem>();
                    pedido.forEach( linha -> {



                        if(Integer.parseInt(linha.substring(0,1)) == 2) {
                            invoice.setNumber(linha.substring(1,11).trim());

                        }
                        if(Integer.parseInt(linha.substring(0,1)) == 3) {
                            invoice.setCustomerCNPJ(linha.substring(1,15).trim());

                        }
                        if(Integer.parseInt(linha.substring(0,1)) == 5) {
                            int year = Integer.parseInt(linha.substring(1,5));
                            int month = Integer.parseInt(linha.substring(5,7));
                            int day = Integer.parseInt(linha.substring(7,9));
                            LocalDate data = LocalDate.of(year,month,day);
                            invoice.setDate(data);

                        }

                        if(Integer.parseInt(linha.substring(0,1)) == 7) {
                            InvoiceItem item = new InvoiceItem();

                            item.setItemCode(linha.substring(1,11).trim());
                            item.setQuantity(Double.parseDouble(linha.substring(11,16).trim()));
                            item.setIdItemConsys(linha.substring(16,27).trim());
                            item.setItemStatus("1");

                            items.add(item);

                        }

                        invoice.setOperationDate(LocalDateTime.now());
                        invoice.setStatus("1");

                    });

                    invoice.setItems(items);
                    pedidos.add(invoice);
                    }
                );



            } catch (IOException e) {
                e.printStackTrace();
            } catch (NumberFormatException e){
                System.out.println("Caractere de Controle");
            }

        }

        return pedidos;





    }


}
