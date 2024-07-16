package br.com.alura.desafioliteralura.view;

import br.com.alura.desafioliteralura.dto.LivrariaDto;
import br.com.alura.desafioliteralura.dto.LivroDto;
import br.com.alura.desafioliteralura.model.Autor;
import br.com.alura.desafioliteralura.model.Livro;
import br.com.alura.desafioliteralura.repository.AutorRepository;
import br.com.alura.desafioliteralura.repository.LivroRepository;
import br.com.alura.desafioliteralura.service.ApiLivro;
import br.com.alura.desafioliteralura.service.DateConvert;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class UserInterface {
    ApiLivro consumoApi = new ApiLivro();
    DateConvert converteJson = new DateConvert();
    String URL = "https://gutendex.com/books/?search=";
    Scanner scanner = new Scanner(System.in);
    int menuNumber = 0;

    private LivroRepository repository;
    private AutorRepository autorRepository;
    private LivroDto livroDto;

    public UserInterface(LivroRepository repository, AutorRepository autorRepository) {
        this.repository = repository;
        this.autorRepository = autorRepository;
    }

    public void printMenu() {
        var opcao = 0 ;
        while (menuNumber != 6) {
            System.out.println("*** Escolha o número de sua opção: *** ");
            System.out.println("1- Buscar livro pelo título");
            System.out.println("2- Listar livros registrados");
            System.out.println("3- Listar autores registrados");
            System.out.println("4- Listar autores vivos em um determinado ano");
            System.out.println("5- Listar livros em um determinado idioma ");
            System.out.println("6 - Sair");
            System.out.println("Selecione uma opção");
            opcao = scanner.nextInt();
            scanner.nextLine();

            if (opcao == 1) {
                System.out.println("Digite o título do livro");
                scanner.nextLine();
                var tituloLivro = scanner.nextLine();
                SelectLivroFromApi(tituloLivro);
            }
            if (opcao == 2) {
                System.out.println("Livros registrados:");
                SelectAllLivro();
            }

            if (opcao == 3) {
                System.out.println("Autores registrados:");
                SelectAllAutor();
            }

            if (opcao == 4) {
                System.out.println("Digite o ano de falecimento:");
                Integer anoSelected = scanner.nextInt();

                if (anoSelected <= 0) {
                    System.out.println("Ano inválido");
                } else {
                    SelectAutorVivos(anoSelected);
                }
            }

            if (opcao == 5) {
                System.out.println("1 = PORTUGUES");
                System.out.println("2 = INGLES");
                System.out.println("SELECIONE O IDIOMA:");
                int idiomaSelected = scanner.nextInt();

                if (idiomaSelected >= 1 || idiomaSelected <= 2) {
                    SelectIdiomaByName(idiomaSelected);
                } else {
                    System.out.println("Idioma selecionado inválido");
                }
            }
        }
    }
    private void InsertLivro(LivroDto livroDados) {
        Optional<Livro> verificarLivro = repository.findByTituloEqualsIgnoreCase(livroDados.titulo().toString());
        if (verificarLivro.isEmpty()) {
            Livro livro = new Livro(livroDados);
            try {
                repository.save(livro);
                System.out.println(livroDados.titulo().toString() + "inserido com sucesso!");
                System.out.println("-----------------------------------------");
            } catch (Exception e){
                System.out.println("Erro: " + e.getMessage());
            }
        } else {
            System.out.println("Livro já cadastrado");
        }
    }

    private void InsertAutor(Integer id, String nome, Integer anoNascimento, Integer anoFalecimento) {
        Autor autor = new Autor();

        try {
            autorRepository.save(autor);
            System.out.println(autor.getNome()+" inserido com sucesso!");
            System.out.println("----------------------------------");

        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
    private void SelectLivroFromApi(String livrotitulo) {
        String datajson = consumoApi.obterDados(URL+livrotitulo.replace(" ","%20").toLowerCase());

        var books = converteJson.obterDados(datajson, LivrariaDto.class);

        Optional<LivroDto> livroSelecionado = books.livros().stream()
                .findFirst();

        if (livroSelecionado.isPresent()){
            LivroDto livrodados = livroSelecionado.get();

            InsertLivro(livrodados);
            InsertAutor(livrodados.id(),livrodados.autores().get(0).nome().toString(),
                    livrodados.autores().get(0).anoNascimento(),livrodados.autores().get(0).anoFalecimento());
        }
        else{
            System.out.println("Nenhum livro encontrado");
        }
    }
    private void SelectAllLivro() {

        try {
            List<Livro> livros = repository.findAll();

            if (livros.size() > 0){
                livros.forEach(System.out::println);
            }else{
                System.out.println("Nenhum livro registrado");
            }

        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
    private void SelectAllAutor() {

        try {
            List<Autor> autores = autorRepository.findAll();

            if (autores.size() > 0){
                autores.forEach(System.out::println);
            }else{
                System.out.println("Nenhum autor registrado");
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
    private void SelectAutorVivos(Integer anoFalecimento) {
        List<Autor> autorData = autorRepository.findByanoFalecimentoLessThan(anoFalecimento);

        if (!autorData.isEmpty()) {
            System.out.println(autorData);
            System.out.println("Total de autores falecidos: "+autorData.size());
        } else {
            System.out.println("Nenhum autor falecido.");
        }
    }
    private void SelectIdiomaByName(int idiomaSelect) {
        var idioma = "";

        if (idiomaSelect == 1)
            idioma = "br";
        else
            idioma = "en";

        List<Livro> livroIdioma = repository.findByIdiomaContainingIgnoreCase(idioma);

        if (!livroIdioma.isEmpty()) {
            System.out.println(livroIdioma);
            System.out.println("Total de livros: " + livroIdioma.size());
        } else {
            System.out.println("Nenhum livro encontrado neste idioma.");
        }
    }
}
