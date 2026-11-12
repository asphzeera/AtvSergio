package org.example.service;

import org.example.model.*;

import java.util.*;

/**
 * Fábrica de dados mocados em memória do Porto de Santos (APS FIPS).
 * Popula os 60 berços comerciais, 11 categorias APS, 1.440 leituras de marés (Novembro/2026
 * com marés críticas <= 0,4m nos dias 08, 14 e 22), frota de navios mercantes e escalas iniciais.
 */
public final class SantosPortDataFactory {

    private SantosPortDataFactory() {
    }

    /**
     * Cria os 60 berços comerciais oficiais do Porto de Santos conforme o DERS/APS.
     * Os primeiros 15 berços são organizados para evidenciar os lotes das Páginas 1, 2 e 3 (RF001 a RF004):
     * - Página 1 (01 a 05): Corredor de Exportação / Granéis Sólidos Vegetais (SV-01 a SV-05)
     * - Página 2 (06 a 10): Terminais Graneleiros e Privados TIPLAM/Cutrale (SV-06 a SV-07, MN-01 a MN-02, SC-01)
     * - Página 3 (11 a 15): Terminais de Contêineres Tecon / BTP e Celulose (CT-01 a CT-04, CL-01)
     * - Páginas 4 a 12 (16 a 60): Completam os 60 berços comerciais de Santos.
     */
    public static List<Berth> createInitial60Berths() {
        List<Berth> berths = new ArrayList<>(60);

        // LOTE PÁGINA 1 (Berços 01 a 05) — RF001 e RF002: Corredor de Exportação (Ponta da Praia / Outeirinhos)
        berths.add(new Berth("SV-01", "Term. XXXIX — Ponta da Praia", "Margem Direita - Corredor Exp.", 14.5, CargoCategory.GRANEIS_SOLIDOS_VEGETAIS, 18000, 310));
        berths.add(new Berth("SV-02", "ADM / Cofco — Armazém 38", "Margem Direita - Corredor Exp.", 14.5, CargoCategory.GRANEIS_SOLIDOS_VEGETAIS, 17500, 300));
        berths.add(new Berth("SV-03", "Cargill / LDC — Ponta da Praia", "Margem Direita - Corredor Exp.", 15.0, CargoCategory.GRANEIS_SOLIDOS_VEGETAIS, 19000, 320));
        berths.add(new Berth("SV-04", "Tes — Terminal Exportador Santos", "Margem Direita - Corredor Exp.", 14.2, CargoCategory.GRANEIS_SOLIDOS_VEGETAIS, 16500, 295));
        berths.add(new Berth("SV-05", "TGG — Terminal de Granéis do Guarujá", "Margem Esquerda - Conceiçãozinha", 14.8, CargoCategory.GRANEIS_SOLIDOS_VEGETAIS, 18500, 315));

        // LOTE PÁGINA 2 (Berços 06 a 10) — RF003: Terminais Graneleiros e Privados
        berths.add(new Berth("SV-06", "Bunge — Terminal Macuco Grãos", "Margem Direita - Macuco", 14.2, CargoCategory.GRANEIS_SOLIDOS_VEGETAIS, 16000, 285));
        berths.add(new Berth("SV-07", "Copersucar / Rumo — Açúcar Outeirinhos", "Margem Direita - Outeirinhos", 14.3, CargoCategory.GRANEIS_SOLIDOS_VEGETAIS, 15500, 290));
        berths.add(new Berth("MN-01", "TIPLAM — Berço 1 (VLI Fertilizantes)", "Margem Direita - Piaçaguera", 13.5, CargoCategory.GRANEIS_MINERAIS, 14000, 270));
        berths.add(new Berth("MN-02", "TIPLAM — Berço 2 (VLI Minerais)", "Margem Direita - Piaçaguera", 13.5, CargoCategory.GRANEIS_MINERAIS, 14000, 270));
        berths.add(new Berth("SC-01", "Cutrale — Terminal Sucocítrico Privado", "Margem Esquerda - Vicente de Carvalho", 13.0, CargoCategory.SUCOS_CITRICOS, 12000, 250));

        // LOTE PÁGINA 3 (Berços 11 a 15) — RF004: Terminais de Contêineres Tecon / BTP
        berths.add(new Berth("CT-01", "Santos Brasil — Tecon Berço 1", "Margem Esquerda - Guarujá", 15.0, CargoCategory.CONTEINERES, 22000, 366));
        berths.add(new Berth("CT-02", "Santos Brasil — Tecon Berço 2", "Margem Esquerda - Guarujá", 15.0, CargoCategory.CONTEINERES, 22000, 366));
        berths.add(new Berth("CT-03", "BTP — Brasil Terminal Portuário Berço 1", "Margem Direita - Alemoa", 15.0, CargoCategory.CONTEINERES, 21000, 355));
        berths.add(new Berth("CT-04", "BTP — Brasil Terminal Portuário Berço 2", "Margem Direita - Alemoa", 15.0, CargoCategory.CONTEINERES, 21000, 355));
        berths.add(new Berth("CT-05", "DP World Santos — Berço Contêineres", "Margem Esquerda - Conceiçãozinha", 15.0, CargoCategory.CONTEINERES, 20000, 350));

        // LOTE Berços 16 a 60 — Completando os 60 berços comerciais oficiais da APS
        berths.add(new Berth("SV-08", "T-Grão Cargo — Outeirinhos", "Margem Direita - Outeirinhos", 14.2, CargoCategory.GRANEIS_SOLIDOS_VEGETAIS, 15000, 280));
        berths.add(new Berth("SV-09", "Cofco International — STS11", "Margem Direita - Paquetá", 14.5, CargoCategory.GRANEIS_SOLIDOS_VEGETAIS, 17000, 295));
        berths.add(new Berth("SV-10", "Viterra / Glencore — Saboó Grãos", "Margem Direita - Saboó", 14.2, CargoCategory.GRANEIS_SOLIDOS_VEGETAIS, 15500, 280));
        berths.add(new Berth("SV-11", "Terminal Trigo Moinho Paulista", "Margem Direita - Outeirinhos", 14.2, CargoCategory.GRANEIS_SOLIDOS_VEGETAIS, 13000, 260));
        berths.add(new Berth("SV-12", "Rumo / Elevações Portuárias A16", "Margem Direita - Outeirinhos", 14.4, CargoCategory.GRANEIS_SOLIDOS_VEGETAIS, 16000, 285));
        berths.add(new Berth("SV-13", "TEAG — Terminal Exportador de Açúcar", "Margem Esquerda - Guarujá", 14.6, CargoCategory.GRANEIS_SOLIDOS_VEGETAIS, 16500, 290));
        berths.add(new Berth("SV-14", "TEV/Saboó — Granéis Agrícolas Sul", "Margem Direita - Saboó", 14.8, CargoCategory.GRANEIS_SOLIDOS_VEGETAIS, 17000, 300));

        berths.add(new Berth("MN-03", "TIPLAM — Berço 3 (Enxofre/Fosfato)", "Margem Direita - Piaçaguera", 13.0, CargoCategory.GRANEIS_MINERAIS, 13500, 265));
        berths.add(new Berth("MN-04", "Mosaic Fertilizantes — Terminal Fospar", "Margem Direita - Saboó", 12.5, CargoCategory.GRANEIS_MINERAIS, 12500, 255));
        berths.add(new Berth("MN-05", "Yara Brasil — Terminal de Fertilizantes", "Margem Direita - Outeirinhos", 13.2, CargoCategory.GRANEIS_MINERAIS, 13000, 260));
        berths.add(new Berth("MN-06", "Termag — Terminal Marítimo do Guarujá", "Margem Esquerda - Conceiçãozinha", 13.5, CargoCategory.GRANEIS_MINERAIS, 14000, 275));
        berths.add(new Berth("MN-07", "Cais do Saboó — Granéis Minerais 01", "Margem Direita - Saboó", 12.8, CargoCategory.GRANEIS_MINERAIS, 11500, 245));
        berths.add(new Berth("MN-08", "Terminal de Sal Marítimo — Outeirinhos", "Margem Direita - Outeirinhos", 12.5, CargoCategory.GRANEIS_MINERAIS, 11000, 240));

        berths.add(new Berth("SC-02", "Citrosuco — Terminal Outeirinhos Norte", "Margem Direita - Outeirinhos", 13.0, CargoCategory.SUCOS_CITRICOS, 12500, 250));
        berths.add(new Berth("SC-03", "Louis Dreyfus Juice — Terminal Cítrico", "Margem Direita - Macuco", 13.0, CargoCategory.SUCOS_CITRICOS, 12000, 245));

        berths.add(new Berth("PS-01", "Concais — Terminal Marítimo de Passageiros", "Margem Direita - Outeirinhos", 12.5, CargoCategory.PASSAGEIROS_CRUZEIROS, 10000, 340));
        berths.add(new Berth("PS-02", "Concais — Berço Auxiliar Transatlânticos", "Margem Direita - Outeirinhos", 12.5, CargoCategory.PASSAGEIROS_CRUZEIROS, 10000, 320));

        berths.add(new Berth("OF-01", "Base de Apoio Offshore — Pré-Sal Bacia Santos", "Margem Direita - Macuco", 13.5, CargoCategory.CARGA_OFFSHORE, 9500, 220));
        berths.add(new Berth("OF-02", "Petrobras Logística — Supridores Offshore", "Margem Direita - Valongo", 13.0, CargoCategory.CARGA_OFFSHORE, 9000, 210));

        berths.add(new Berth("CT-06", "Santos Brasil — Tecon Berço 3", "Margem Esquerda - Guarujá", 14.5, CargoCategory.CONTEINERES, 19500, 340));
        berths.add(new Berth("CT-07", "Santos Brasil — Tecon Berço 4 (Cabotagem)", "Margem Esquerda - Guarujá", 13.8, CargoCategory.CONTEINERES, 18000, 310));
        berths.add(new Berth("CT-08", "BTP — Brasil Terminal Portuário Berço 3", "Margem Direita - Alemoa", 14.8, CargoCategory.CONTEINERES, 20000, 345));
        berths.add(new Berth("CT-09", "Ecoporto Santos — Berço Contêiner 1", "Margem Direita - Saboó", 13.5, CargoCategory.CONTEINERES, 16000, 290));
        berths.add(new Berth("CT-10", "STS10 — Novo Terminal Multicargas/Box", "Margem Direita - Saboó", 14.5, CargoCategory.CONTEINERES, 19000, 330));

        berths.add(new Berth("CL-01", "Eldorado Brasil — Terminal STS14 Celulose", "Margem Direita - Macuco", 14.5, CargoCategory.CELULOSE, 16000, 300));
        berths.add(new Berth("CL-02", "Bracell — Terminal STS14A Celulose", "Margem Direita - Macuco", 14.5, CargoCategory.CELULOSE, 16500, 300));
        berths.add(new Berth("CL-03", "Suzano Papel e Celulose — T32", "Margem Direita - Macuco", 14.2, CargoCategory.CELULOSE, 15500, 290));
        berths.add(new Berth("CL-04", "DP World Celulose — Berço Dedicado Pulp", "Margem Esquerda - Conceiçãozinha", 14.5, CargoCategory.CELULOSE, 16000, 305));
        berths.add(new Berth("CL-05", "Marimex / Fibria — Retroporto Celulose", "Margem Direita - Outeirinhos", 13.8, CargoCategory.CELULOSE, 14000, 275));

        berths.add(new Berth("LQ-01", "Alemoa — Pier Petroleiro e GLP 01", "Margem Direita - Alemoa", 13.8, CargoCategory.LIQUIDOS_QUIMICOS_COMBUSTIVEIS, 15000, 260));
        berths.add(new Berth("LQ-02", "Alemoa — Pier Líquidos inflamáveis 02", "Margem Direita - Alemoa", 13.8, CargoCategory.LIQUIDOS_QUIMICOS_COMBUSTIVEIS, 15000, 260));
        berths.add(new Berth("LQ-03", "Ilha Barnabé — Stolthaven / Ultracargo B1", "Margem Esquerda - Ilha Barnabé", 13.2, CargoCategory.LIQUIDOS_QUIMICOS_COMBUSTIVEIS, 12500, 240));
        berths.add(new Berth("LQ-04", "Ilha Barnabé — Vopak / Adonai Químicos B2", "Margem Esquerda - Ilha Barnabé", 13.2, CargoCategory.LIQUIDOS_QUIMICOS_COMBUSTIVEIS, 12500, 240));
        berths.add(new Berth("LQ-05", "Ilha Barnabé — Ageo Terminais Berço Sul", "Margem Esquerda - Ilha Barnabé", 13.5, CargoCategory.LIQUIDOS_QUIMICOS_COMBUSTIVEIS, 13000, 250));
        berths.add(new Berth("LQ-06", "Transpetro — Pier de Derivados Alemoa", "Margem Direita - Alemoa", 14.0, CargoCategory.LIQUIDOS_QUIMICOS_COMBUSTIVEIS, 16000, 275));
        berths.add(new Berth("LQ-07", "Copape / Raízen — Terminal de Etanol", "Margem Direita - Alemoa", 13.4, CargoCategory.LIQUIDOS_QUIMICOS_COMBUSTIVEIS, 13500, 245));
        berths.add(new Berth("LQ-08", "Granel Química — STS08A Combustíveis", "Margem Direita - Alemoa", 14.0, CargoCategory.LIQUIDOS_QUIMICOS_COMBUSTIVEIS, 15500, 270));

        berths.add(new Berth("SD-01", "Usiminas Cubatão — TUP Siderúrgico B1", "Canal de Piaçaguera - Cubatão", 13.0, CargoCategory.PRODUTOS_SIDERURGICOS, 11000, 250));
        berths.add(new Berth("SD-02", "Usiminas Cubatão — TUP Bobinas/Chapas B2", "Canal de Piaçaguera - Cubatão", 13.0, CargoCategory.PRODUTOS_SIDERURGICOS, 11000, 250));
        berths.add(new Berth("SD-03", "Cais do Saboó — Carga Geral Siderúrgica", "Margem Direita - Saboó", 12.8, CargoCategory.PRODUTOS_SIDERURGICOS, 10500, 235));

        berths.add(new Berth("RR-01", "TEV Santos Brasil — Terminal de Veículos B1", "Margem Esquerda - Guarujá", 13.8, CargoCategory.VEICULOS_RORO, 9000, 265));
        berths.add(new Berth("RR-02", "Cais de Outeirinhos — Ro-Ro Exportação", "Margem Direita - Outeirinhos", 13.2, CargoCategory.VEICULOS_RORO, 8500, 250));

        berths.add(new Berth("MP-01", "Ecoporto — Terminal Multipropósito e Projeto", "Margem Direita - Valongo", 13.0, CargoCategory.MULTIPROPOSITO, 10000, 245));
        berths.add(new Berth("MP-02", "Cais Público Macuco — Carga de Projeto", "Margem Direita - Macuco", 13.2, CargoCategory.MULTIPROPOSITO, 10500, 255));
        berths.add(new Berth("MP-03", "Deicmar / Porto Novo — Cais Paquetá", "Margem Direita - Paquetá", 12.8, CargoCategory.MULTIPROPOSITO, 9500, 235));

        return berths;
    }

    /**
     * Cria o Catálogo de Cargas e Perfis Portuários APS (RF007) contemplando todas as 11 categorias.
     */
    public static List<CargoItem> createInitialCargoCatalog() {
        List<CargoItem> items = new ArrayList<>();
        items.add(new CargoItem("APS-SV01", "Soja em Grãos (Exportação)", CargoCategory.GRANEIS_SOLIDOS_VEGETAIS, "↑ Carga", 18500, "Toneladas Métricas (t)"));
        items.add(new CargoItem("APS-SV02", "Açúcar VHP Granel", CargoCategory.GRANEIS_SOLIDOS_VEGETAIS, "↑ Carga", 16000, "Toneladas Métricas (t)"));
        items.add(new CargoItem("APS-SV03", "Milho em Grãos", CargoCategory.GRANEIS_SOLIDOS_VEGETAIS, "↑ Carga", 17500, "Toneladas Métricas (t)"));
        items.add(new CargoItem("APS-SV04", "Trigo em Grãos (Importação)", CargoCategory.GRANEIS_SOLIDOS_VEGETAIS, "↓ Descarga", 13000, "Toneladas Métricas (t)"));
        items.add(new CargoItem("APS-CT01", "Contêineres Full / Reefer (Longo Curso)", CargoCategory.CONTEINERES, "↓↑ Misto", 22000, "Toneladas Equivalentes TEU"));
        items.add(new CargoItem("APS-CL01", "Celulose Branqueada de Eucalipto (Fardos)", CargoCategory.CELULOSE, "↑ Carga", 16000, "Toneladas Métricas (t)"));
        items.add(new CargoItem("APS-LQ01", "Óleo Diesel Marítimo / Derivados e Etanol", CargoCategory.LIQUIDOS_QUIMICOS_COMBUSTIVEIS, "↓↑ Misto", 15000, "Toneladas Líquidas (t)"));
        items.add(new CargoItem("APS-LQ02", "Soda Cáustica e Químicos (Ilha Barnabé)", CargoCategory.LIQUIDOS_QUIMICOS_COMBUSTIVEIS, "↓ Descarga", 12500, "Toneladas Líquidas (t)"));
        items.add(new CargoItem("APS-SD01", "Bobinas e Chapas de Aço Laminado", CargoCategory.PRODUTOS_SIDERURGICOS, "↑ Carga", 11000, "Toneladas Métricas (t)"));
        items.add(new CargoItem("APS-MN01", "Fertilizantes NPK / Cloreto de Potássio", CargoCategory.GRANEIS_MINERAIS, "↓ Descarga", 14000, "Toneladas Métricas (t)"));
        items.add(new CargoItem("APS-MN02", "Enxofre e Rocha Fosfática Granel", CargoCategory.GRANEIS_MINERAIS, "↓ Descarga", 13500, "Toneladas Métricas (t)"));
        items.add(new CargoItem("APS-SC01", "Suco de Laranja Concentrado Congelado (FCOJ)", CargoCategory.SUCOS_CITRICOS, "↑ Carga", 12000, "Toneladas Tanker (t)"));
        items.add(new CargoItem("APS-MP01", "Carga Geral Solta e Maquinário Pesado", CargoCategory.MULTIPROPOSITO, "↓↑ Misto", 10000, "Toneladas Métricas (t)"));
        items.add(new CargoItem("APS-RR01", "Automóveis, Caminhões e Tratores (Ro-Ro)", CargoCategory.VEICULOS_RORO, "↓↑ Misto", 9000, "Toneladas Equivalentes Ro-Ro"));
        items.add(new CargoItem("APS-PS01", "Cruzeiros Marítimos Internacionais (Bagagem/Supr.)", CargoCategory.PASSAGEIROS_CRUZEIROS, "↓↑ Misto", 10000, "Toneladas Operacionais"));
        items.add(new CargoItem("APS-OF01", "Tubos Flexíveis e Suprimentos Pré-Sal", CargoCategory.CARGA_OFFSHORE, "↑ Carga", 9500, "Toneladas Offshore"));
        return items;
    }

    /**
     * Cria a frota inicial de navios mercantes (RF008 e RF009) com calados entre 11,5m e 14,5m.
     */
    public static List<RegisteredVessel> createInitialVessels() {
        List<RegisteredVessel> vessels = new ArrayList<>();
        vessels.add(new RegisteredVessel("IMO-9812345", "MV Santos Sovereign", "Capesize", 14.4, 115000, CargoCategory.GRANEIS_SOLIDOS_VEGETAIS, "Panamá"));
        vessels.add(new RegisteredVessel("IMO-9745120", "MV Cerrado Star", "Post-Panamax", 14.1, 88000, CargoCategory.GRANEIS_SOLIDOS_VEGETAIS, "Libéria"));
        vessels.add(new RegisteredVessel("IMO-9632189", "MV Brasilia Grain", "Panamax", 13.8, 76000, CargoCategory.GRANEIS_SOLIDOS_VEGETAIS, "Ilhas Marshall"));
        vessels.add(new RegisteredVessel("IMO-9587412", "MV Paranaguá Express", "Panamax", 13.2, 68000, CargoCategory.GRANEIS_SOLIDOS_VEGETAIS, "Singapura"));
        vessels.add(new RegisteredVessel("IMO-9498765", "MV Pampas Harvester", "Supramax", 12.6, 58000, CargoCategory.GRANEIS_SOLIDOS_VEGETAIS, "Malta"));

        vessels.add(new RegisteredVessel("IMO-9901122", "MSC Ipanema Giant", "New Panamax", 14.5, 132000, CargoCategory.CONTEINERES, "Panamá"));
        vessels.add(new RegisteredVessel("IMO-9876543", "Maersk Monte Pascoal", "Post-Panamax", 14.2, 118000, CargoCategory.CONTEINERES, "Dinamarca"));
        vessels.add(new RegisteredVessel("IMO-9834561", "CMA CGM Amazonas", "Post-Panamax", 13.9, 105000, CargoCategory.CONTEINERES, "França"));
        vessels.add(new RegisteredVessel("IMO-9711234", "Log-In Jatobá", "Feeder Container", 12.2, 42000, CargoCategory.CONTEINERES, "Brasil"));

        vessels.add(new RegisteredVessel("IMO-9654321", "MV Tiplam Pioneer", "Supramax", 13.1, 61000, CargoCategory.GRANEIS_MINERAIS, "Chipre"));
        vessels.add(new RegisteredVessel("IMO-9543218", "MV Baltic Potash", "Handymax", 12.4, 48000, CargoCategory.GRANEIS_MINERAIS, "Noruega"));

        vessels.add(new RegisteredVessel("IMO-9432190", "MT Orange Blossom", "Juice Tanker", 12.5, 44000, CargoCategory.SUCOS_CITRICOS, "Libéria"));
        vessels.add(new RegisteredVessel("IMO-9688774", "MV Green Eucalyptus", "Open Hatch Pulp", 13.9, 72000, CargoCategory.CELULOSE, "Singapura"));
        vessels.add(new RegisteredVessel("IMO-9512367", "MT Alemoa Voyager", "Chemical Tanker", 13.0, 52000, CargoCategory.LIQUIDOS_QUIMICOS_COMBUSTIVEIS, "Bahamas"));
        vessels.add(new RegisteredVessel("IMO-9478523", "MV Usiminas Steel", "Handymax", 12.1, 46000, CargoCategory.PRODUTOS_SIDERURGICOS, "Hong Kong"));
        vessels.add(new RegisteredVessel("IMO-9765432", "MV Glovis South America", "Ro-Ro Pure Car Carrier", 11.8, 35000, CargoCategory.VEICULOS_RORO, "Coreia do Sul"));
        vessels.add(new RegisteredVessel("IMO-9823456", "MSC Seaview Cruise", "Cruise Liner", 11.5, 38000, CargoCategory.PASSAGEIROS_CRUZEIROS, "Malta"));
        vessels.add(new RegisteredVessel("IMO-9799881", "MV Skandi Búzios", "Offshore Supply", 12.0, 28000, CargoCategory.CARGA_OFFSHORE, "Brasil"));

        return vessels;
    }

    /**
     * Gera a Tábua de Marés Astronômicas de 30 dias x 48 medições de 30 minutos = 1.440 leituras (RF010).
     * Em "Novembro de 2026", garante marés baixas críticas (<= 0,4m) especificamente nos dias 08, 14 e 22.
     */
    public static Map<String, TideTable> createTideTables() {
        Map<String, TideTable> tables = new LinkedHashMap<>();
        tables.put("Novembro de 2026", buildTideTableForMonth("Novembro de 2026", Set.of(8, 14, 22)));
        tables.put("Setembro de 2026", buildTideTableForMonth("Setembro de 2026", Set.of(5, 13, 21)));
        tables.put("Junho de 2026", buildTideTableForMonth("Junho de 2026", Set.of(6, 15, 24)));
        return tables;
    }

    private static TideTable buildTideTableForMonth(String monthYear, Set<Integer> criticalDays) {
        List<TideMeasurement> measurements = new ArrayList<>(1440);
        int seq = 1;

        for (int day = 1; day <= 30; day++) {
            boolean isCriticalDay = criticalDays.contains(day);
            for (int slot = 0; slot < 48; slot++) {
                int hour = slot / 2;
                int minute = (slot % 2) * 30;
                String timeFormatted = String.format("%02d:%02d", hour, minute);

                // Ciclo semidiurno (~12,5 horas = 25 slots de 30 min)
                double angle = (slot / 24.0) * 2.0 * Math.PI + (day * 0.35);
                double cosine = Math.cos(angle);

                double height;
                if (isCriticalDay) {
                    // Nos dias críticos (08, 14 e 22), a baixa-mar atinge entre 0,18m e 0,38m (<= 0,4m)
                    if (slot >= 10 && slot <= 14) {
                        height = 0.24 + 0.04 * (slot - 12) * (slot - 12); // ~0.24m a 0.40m
                    } else if (slot >= 34 && slot <= 38) {
                        height = 0.28 + 0.03 * (slot - 36) * (slot - 36); // ~0.28m a 0.40m
                    } else {
                        height = Math.max(0.45, 1.05 + 0.62 * cosine);
                    }
                } else {
                    // Nos dias normais, a maré varia entre 0,55m e 1,75m (sempre > 0,4m)
                    height = Math.max(0.55, 1.15 + 0.52 * cosine);
                }

                height = Math.round(height * 100.0) / 100.0;

                String state;
                if (height <= TideMeasurement.CRITICAL_LOW_TIDE_LIMIT) {
                    state = "Baixa Crítica (≤ 0,4m)";
                } else if (height >= 1.45) {
                    state = "Preamar (Maré Alta)";
                } else if (Math.sin(angle) < 0) {
                    state = "Maré Vazante";
                } else {
                    state = "Maré Enchente";
                }

                measurements.add(new TideMeasurement(seq++, day, monthYear, timeFormatted, height, state));
            }
        }

        return new TideTable(monthYear, measurements);
    }

    /**
     * Cria as escalas iniciais de navios pré-carregadas para que o Lineup abra rico e preenchido
     * nas Páginas 1 (Berços 1-5), 2 (Berços 6-10) e 3 (Berços 11-15), respeitando RN001, RN002 e RN003.
     */
    public static List<ShipSchedule> createInitialSchedules(List<Berth> berths, List<RegisteredVessel> vessels) {
        Map<String, Berth> berthMap = new HashMap<>();
        berths.forEach(b -> berthMap.put(b.getCode(), b));

        Map<String, RegisteredVessel> vesselMap = new HashMap<>();
        vessels.forEach(v -> vesselMap.put(v.getImoCode(), v));

        List<ShipSchedule> list = new ArrayList<>();

        // PÁGINA 1 (Berços 01 a 05 — SV-01 a SV-05)
        // Nota: Navios com calado > 13,5m não iniciam nem terminam nos dias 08, 14 ou 22 (RN002)
        list.add(new ShipSchedule("ESC-2026-001", berthMap.get("SV-01"), vesselMap.get("IMO-9812345"),
                2, 7, 90000, ShipSchedule.STATUS_OPERANDO, "Soja em Grãos (Exportação)", "Wilson Sons Agência"));
        list.add(new ShipSchedule("ESC-2026-002", berthMap.get("SV-01"), vesselMap.get("IMO-9632189"),
                10, 13, 54000, ShipSchedule.STATUS_CONFIRMADO, "Milho em Grãos", "Cargonave Marítima"));
        list.add(new ShipSchedule("ESC-2026-003", berthMap.get("SV-01"), vesselMap.get("IMO-9745120"),
                16, 21, 88000, ShipSchedule.STATUS_PROGRAMADO, "Soja em Grãos (Exportação)", "Orion Rodrimar"));

        list.add(new ShipSchedule("ESC-2026-004", berthMap.get("SV-02"), vesselMap.get("IMO-9745120"),
                3, 7, 70000, ShipSchedule.STATUS_CONFIRMADO, "Açúcar VHP Granel", "Lachmann Agenciamento"));
        list.add(new ShipSchedule("ESC-2026-005", berthMap.get("SV-02"), vesselMap.get("IMO-9587412"),
                9, 13, 68000, ShipSchedule.STATUS_OPERANDO, "Soja em Grãos (Exportação)", "Williams Brasil"));
        list.add(new ShipSchedule("ESC-2026-006", berthMap.get("SV-02"), vesselMap.get("IMO-9498765"),
                18, 22, 58000, ShipSchedule.STATUS_PROGRAMADO, "Farelo de Soja", "Unimar Agenciamentos")); // Calado 12.6m (permitido dia 22)

        list.add(new ShipSchedule("ESC-2026-007", berthMap.get("SV-03"), vesselMap.get("IMO-9632189"),
                1, 5, 75000, ShipSchedule.STATUS_OPERANDO, "Soja em Grãos (Exportação)", "Wilson Sons Agência"));
        list.add(new ShipSchedule("ESC-2026-008", berthMap.get("SV-03"), vesselMap.get("IMO-9812345"),
                10, 16, 110000, ShipSchedule.STATUS_CONFIRMADO, "Milho em Grãos", "GAC Brasil"));
        list.add(new ShipSchedule("ESC-2026-009", berthMap.get("SV-03"), vesselMap.get("IMO-9587412"),
                23, 28, 68000, ShipSchedule.STATUS_PROGRAMADO, "Açúcar VHP Granel", "Alphamar Agência"));

        list.add(new ShipSchedule("ESC-2026-010", berthMap.get("SV-04"), vesselMap.get("IMO-9498765"),
                4, 8, 56000, ShipSchedule.STATUS_CONFIRMADO, "Trigo em Grãos", "Cargonave Marítima")); // Calado 12.6m (permitido dia 8)
        list.add(new ShipSchedule("ESC-2026-011", berthMap.get("SV-04"), vesselMap.get("IMO-9587412"),
                15, 19, 64000, ShipSchedule.STATUS_PROGRAMADO, "Soja em Grãos (Exportação)", "LBH Brasil"));

        list.add(new ShipSchedule("ESC-2026-012", berthMap.get("SV-05"), vesselMap.get("IMO-9745120"),
                9, 13, 74000, ShipSchedule.STATUS_OPERANDO, "Soja em Grãos (Exportação)", "Inchcape Shipping"));
        list.add(new ShipSchedule("ESC-2026-013", berthMap.get("SV-05"), vesselMap.get("IMO-9632189"),
                17, 21, 72000, ShipSchedule.STATUS_CONFIRMADO, "Milho em Grãos", "Wilson Sons Agência"));
        list.add(new ShipSchedule("ESC-2026-014", berthMap.get("SV-05"), vesselMap.get("IMO-9812345"),
                24, 29, 92000, ShipSchedule.STATUS_PROGRAMADO, "Soja em Grãos (Exportação)", "Orion Rodrimar"));

        // PÁGINA 2 (Berços 06 a 10 — SV-06, SV-07, MN-01, MN-02, SC-01)
        list.add(new ShipSchedule("ESC-2026-015", berthMap.get("SV-06"), vesselMap.get("IMO-9498765"),
                2, 6, 55000, ShipSchedule.STATUS_OPERANDO, "Soja em Grãos (Exportação)", "Bunge Maritime"));
        list.add(new ShipSchedule("ESC-2026-016", berthMap.get("SV-06"), vesselMap.get("IMO-9587412"),
                12, 16, 62000, ShipSchedule.STATUS_CONFIRMADO, "Açúcar VHP Granel", "Williams Brasil"));

        list.add(new ShipSchedule("ESC-2026-017", berthMap.get("SV-07"), vesselMap.get("IMO-9587412"),
                5, 9, 60000, ShipSchedule.STATUS_CONFIRMADO, "Açúcar VHP Granel", "Copersucar Chartering"));
        list.add(new ShipSchedule("ESC-2026-018", berthMap.get("SV-07"), vesselMap.get("IMO-9498765"),
                19, 23, 52000, ShipSchedule.STATUS_PROGRAMADO, "Açúcar VHP Granel", "Unimar Agenciamentos"));

        list.add(new ShipSchedule("ESC-2026-019", berthMap.get("MN-01"), vesselMap.get("IMO-9654321"),
                3, 7, 56000, ShipSchedule.STATUS_OPERANDO, "Fertilizantes NPK", "VLI Logística Portuária"));
        list.add(new ShipSchedule("ESC-2026-020", berthMap.get("MN-01"), vesselMap.get("IMO-9543218"),
                14, 18, 48000, ShipSchedule.STATUS_CONFIRMADO, "Cloreto de Potássio", "Fertimport S/A")); // Calado 12.4m (permitido dia 14)

        list.add(new ShipSchedule("ESC-2026-021", berthMap.get("MN-02"), vesselMap.get("IMO-9543218"),
                6, 10, 45000, ShipSchedule.STATUS_CONFIRMADO, "Enxofre Granel", "VLI Logística Portuária"));
        list.add(new ShipSchedule("ESC-2026-022", berthMap.get("MN-02"), vesselMap.get("IMO-9654321"),
                20, 25, 60000, ShipSchedule.STATUS_PROGRAMADO, "Rocha Fosfática", "Mosaic Shipping"));

        list.add(new ShipSchedule("ESC-2026-023", berthMap.get("SC-01"), vesselMap.get("IMO-9432190"),
                8, 12, 42000, ShipSchedule.STATUS_OPERANDO, "Suco de Laranja FCOJ", "Cutrale Shipping")); // Calado 12.5m (permitido dia 8)
        list.add(new ShipSchedule("ESC-2026-024", berthMap.get("SC-01"), vesselMap.get("IMO-9432190"),
                23, 27, 40000, ShipSchedule.STATUS_PROGRAMADO, "Suco de Laranja FCOJ", "Cutrale Shipping"));

        // PÁGINA 3 (Berços 11 a 15 — CT-01 a CT-05)
        list.add(new ShipSchedule("ESC-2026-025", berthMap.get("CT-01"), vesselMap.get("IMO-9901122"),
                2, 6, 88000, ShipSchedule.STATUS_OPERANDO, "Contêineres Longo Curso", "MSC Mediterranean"));
        list.add(new ShipSchedule("ESC-2026-026", berthMap.get("CT-01"), vesselMap.get("IMO-9876543"),
                11, 15, 84000, ShipSchedule.STATUS_CONFIRMADO, "Contêineres Reefer/Dry", "Maersk Brasil"));
        list.add(new ShipSchedule("ESC-2026-027", berthMap.get("CT-01"), vesselMap.get("IMO-9834561"),
                23, 27, 80000, ShipSchedule.STATUS_PROGRAMADO, "Contêineres Ásia-Santos", "CMA CGM Brasil"));

        list.add(new ShipSchedule("ESC-2026-028", berthMap.get("CT-02"), vesselMap.get("IMO-9834561"),
                4, 7, 66000, ShipSchedule.STATUS_CONFIRMADO, "Contêineres Europa", "CMA CGM Brasil"));
        list.add(new ShipSchedule("ESC-2026-029", berthMap.get("CT-02"), vesselMap.get("IMO-9711234"),
                14, 17, 42000, ShipSchedule.STATUS_OPERANDO, "Contêineres Cabotagem", "Log-In Logística")); // Calado 12.2m (permitido dia 14)

        list.add(new ShipSchedule("ESC-2026-030", berthMap.get("CT-03"), vesselMap.get("IMO-9876543"),
                3, 7, 82000, ShipSchedule.STATUS_OPERANDO, "Contêineres Full", "Maersk Brasil"));
        list.add(new ShipSchedule("ESC-2026-031", berthMap.get("CT-03"), vesselMap.get("IMO-9901122"),
                16, 20, 90000, ShipSchedule.STATUS_PROGRAMADO, "Contêineres Longo Curso", "MSC Mediterranean"));

        list.add(new ShipSchedule("ESC-2026-032", berthMap.get("CT-04"), vesselMap.get("IMO-9711234"),
                6, 9, 40000, ShipSchedule.STATUS_CONFIRMADO, "Contêineres Mercosul", "Aliança Navegação"));
        list.add(new ShipSchedule("ESC-2026-033", berthMap.get("CT-04"), vesselMap.get("IMO-9834561"),
                17, 21, 78000, ShipSchedule.STATUS_PROGRAMADO, "Contêineres América do Norte", "Hapag-Lloyd"));

        list.add(new ShipSchedule("ESC-2026-034", berthMap.get("CT-05"), vesselMap.get("IMO-9901122"),
                9, 13, 85000, ShipSchedule.STATUS_CONFIRMADO, "Contêineres DP World", "DP World Agency"));
        list.add(new ShipSchedule("ESC-2026-035", berthMap.get("CT-05"), vesselMap.get("IMO-9876543"),
                24, 28, 82000, ShipSchedule.STATUS_PROGRAMADO, "Contêineres Longo Curso", "ONE Ocean Network"));

        return list;
    }
}
