# ⚓ Gêmeo Digital do Porto de Santos (APS • FIPS) — Checkpoint 1

**Disciplina:** Programação Orientada a Objetos (POO) — Avaliação N3 (UniRV)  
**Escopo do Checkpoint 1:** Modal Marítimo (`RF001` a `RF012`) com arquitetura MVC em **JavaFX 21+** e **Dados Mocados em Memória** (`ObservableList`), totalmente desacoplado de banco de dados.

---

## 📋 1. Pré-requisitos

Para compilar e executar o projeto em qualquer sistema operacional (**Windows**, **Linux** ou **macOS**), certifique-se de ter instalado:

1. **Java Development Kit (JDK) 21 ou superior**
   * Verifique no terminal com:
     ```bash
     java -version
     javac -version
     ```
2. **Apache Maven 3.8 ou superior**
   * Verifique no terminal com:
     ```bash
     mvn -version
     ```
3. **Git** (opcional, caso baixe via `git clone`).

---

## 🚀 2. Instalação Passo a Passo por Sistema Operacional

### 🐧 Ubuntu / Debian / Linux Mint
```bash
sudo apt update
sudo apt install -y openjdk-21-jdk maven git
```

### 🎩 Fedora / RHEL
```bash
sudo dnf install -y java-21-openjdk-devel maven git
```

### ❄️ NixOS
```bash
nix-shell -p jdk21 maven git
```

### 🪟 Windows (via Winget ou PowerShell)
```powershell
winget install Microsoft.OpenJDK.21
winget install Apache.Maven
winget install Git.Git
```

### 🍎 macOS (via Homebrew)
```bash
brew install openjdk@21 maven git
```

---

## ▶️ 3. Como Baixar e Executar o Projeto

### Passo 1 — Clonar o repositório (ou extrair o `.zip`)
```bash
git clone https://github.com/asphzeera/AtvSergio.git
cd AtvSergio
```
*(Ou, via SSH: `git clone git@github.com:asphzeera/AtvSergio.git`)*

### Passo 2 — Compilar e executar a aplicação JavaFX
Na raiz do projeto (onde está o arquivo `pom.xml`), execute o comando oficial:

```bash
mvn clean javafx:run
```

O Maven baixará automaticamente as dependências do **OpenJFX 21.0.2**, compilará as classes e abrirá a janela gráfica do **Gêmeo Digital do Porto de Santos (APS • FIPS)**.

---

## 🏗️ 4. Arquitetura MVC e Estrutura do Projeto

O sistema segue rigorosamente o padrão **Model-View-Controller (MVC)** com camada de serviço desacoplada:

```text
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── org/
        │       └── example/
        │           ├── App.java                          # Ponto de entrada JavaFX
        │           ├── controller/
        │           │   └── PortController.java           # Controlador MVC reativo
        │           ├── model/
        │           │   ├── Berth.java                    # Berço comercial e cálculo RN004
        │           │   ├── CargoCategory.java            # Enum com os 11 Perfis de Carga APS
        │           │   ├── CargoItem.java                # Commodity e produtividade nominal
        │           │   ├── RegisteredVessel.java         # Navio mercante e validação IMO (RN007)
        │           │   ├── ShipSchedule.java             # Escala programada, RN001 e cores RN005
        │           │   ├── TideMeasurement.java          # Leitura de maré a cada 30 minutos
        │           │   └── TideTable.java                # Agregado mensal de 1.440 leituras
        │           ├── service/
        │           │   ├── PortDataService.java          # Contrato desacoplado de serviço
        │           │   ├── MockPortDataServiceImpl.java  # Implementação reativa em memória
        │           │   └── SantosPortDataFactory.java    # Fábrica com 60 berços, marés e escalas
        │           └── view/
        │               ├── MainLayout.java               # Layout principal e menu lateral escuro
        │               ├── LineupView.java               # RF001 a RF004 (Lineup, KPIs, Paginação)
        │               ├── BerthChartCard.java           # Card visual de berço com barras e maré
        │               ├── BerthManagementView.java      # RF005 e RF006 (CRUD 60 berços + RN024)
        │               ├── CargoCatalogView.java         # RF007 (Catálogo de Cargas APS)
        │               ├── VesselCatalogView.java        # RF008 e RF009 (Frota de Navios IMO)
        │               ├── TideMonitoringView.java       # RF010 (Tábuas de Marés 30m + Import CSV)
        │               └── ShipScheduleView.java         # RF011 e RF012 (Agendamento + Histórico)
        └── resources/
            ├── styles/
            │   └── main.css                              # Estilização visual APS FIPS
            └── tides/
                └── mare_novembro_2026.csv                # Arquivo CSV de exemplo (DHN/Marinha)
```

---

## ✅ 5. Roteiro Rápido de Auditoria (RF001 a RF012 e Regras de Negócio)

1. **Lineup Portuário Geral (`RF001` a `RF004`):**
   * Confira os **4 KPIs Globais** no topo e os **11 CheckBoxes de Perfil de Carga APS**.
   * Role verticalmente a área central para inspecionar os cartões de berço até o **Berço 05** no rodapé (`RF002`).
   * Utilize os botões de seta (`◀`, `▶`, `⏮`, `⏭`) ou os números de página (`1`, `2`, `3`...) para navegar entre os lotes de 5 em 5 berços (`RF003` e `RF004`).
   * Clique sobre qualquer barra de navio para atualizar o **Banner de Resumo do Navio Selecionado** no rodapé.
2. **Berços de Atracação (`RF005`, `RF006` e `RN024`):**
   * Cadastre ou edite berços no formulário superior e confira a tabela com os **60 berços comerciais de Santos**.
   * Ao excluir um berço, todas as atracações associadas a ele são removidas em cascata (`RN024`) e o Lineup reflete a mudança em tempo real (`RN008`).
3. **Catálogo de Cargas APS (`RF007`):**
   * Gerencie as commodities portuárias, sentido operacional (`↑ Carga`, `↓ Descarga`, `↓↑ Misto`) e taxa diária nominal ($t/\text{dia}$).
4. **Catálogo de Navios (`RF008`, `RF009` e `RN007`):**
   * Cadastre embarcações validando o código internacional obrigatório `IMO-` seguido de dígitos (`RN007`).
5. **Tábuas de Marés (`RF010`):**
   * alterne o mês ativo, visualize o gráfico senoidal com linha de corte em **$0,40\text{ m}$**, inspecione as **1.440 medições de 30 em 30 minutos** ou importe um arquivo `.csv` pelo botão **`📥 Importar CSV/Excel de Marés`** (há um arquivo pronto em `src/main/resources/tides/mare_novembro_2026.csv`).
6. **Programação de Chegadas (`RF011`, `RF012`, `RN001`, `RN002`, `RN003` e `RN004`):**
   * Teste o bloqueio anti-sobreposição temporal no mesmo berço (`RN001`).
   * Teste o bloqueio hidrodinâmico ao tentar agendar um navio com calado $> 13,5\text{ m}$ iniciando ou terminando nos dias críticos **08, 14 ou 22 de Novembro** (`RN002`).
   * Teste a validação de compatibilidade de perfil de carga entre navio e berço (`RN003`) e o cálculo determinístico de duração (`RN004`).
