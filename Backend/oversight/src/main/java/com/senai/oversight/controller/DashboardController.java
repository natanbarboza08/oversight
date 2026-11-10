package com.senai.oversight.controller;                     // pacote desta classe
// ============================================================================
// Porta de entrada dos GRÁFICOS.
// GET /api/dashboard            -> último lote processado + JSON das contas
// GET /api/dashboard?loteId=3   -> um lote específico
// GET /api/dashboard/lotes      -> os últimos lotes (para escolher na tela)
// Exige login (token JWT), como as outras rotas. Chama: DashboardService.
// ============================================================================

import com.senai.oversight.service.DashboardService;                             // import: serviço que lê o banco
import java.util.List;                                                         // import: tipo lista
import java.util.Map;                                                          // import: tipo mapa (chave -> valor)
import org.springframework.web.bind.annotation.GetMapping;                     // import: rota GET
import org.springframework.web.bind.annotation.RequestMapping;                 // import: prefixo das rotas
import org.springframework.web.bind.annotation.RequestParam;                   // import: lê ?loteId= da URL
import org.springframework.web.bind.annotation.RestController;                 // import: controller REST

@RestController                                                                // controller REST dos gráficos
@RequestMapping("/api/dashboard")                                              // rotas começam com /api/dashboard
public class DashboardController {                                             // classe do controller
    private final DashboardService service;                                    // serviço que lê o banco

    public DashboardController(DashboardService service) {                     // construtor: o Spring entrega o serviço
        this.service = service;                                                // guarda o serviço
    }                                                                          // fecha o bloco

    @GetMapping                                                                // GET /api/dashboard (pode ser testada no Thunder Client com o token)
    public Map<String, Object> obter(@RequestParam(required = false) Long loteId) {  // loteId é opcional
        return service.obter(loteId);                                          // devolve { lote, geradoEm, estatisticas }
    }                                                                          // fecha o bloco

    @GetMapping("/lotes")                                                      // GET /api/dashboard/lotes
    public List<Map<String, Object>> lotes() {                                 // lista de lotes
        return service.listarLotes();                                          // os 20 mais recentes
    }                                                                          // fecha o bloco
}                                                                              // fecha o bloco
