package ro.unibuc.prodeng.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.exception.GlobalExceptionHandler;
import ro.unibuc.prodeng.request.CreateSupplierRequest;
import ro.unibuc.prodeng.request.UpdateSupplierRequest;
import ro.unibuc.prodeng.response.SupplierResponse;
import ro.unibuc.prodeng.service.SupplierService;

@ExtendWith(SpringExtension.class)
class SupplierControllerTest {

    @Mock
    private SupplierService supplierService;

    @InjectMocks
    private SupplierController supplierController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(supplierController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testGetSupplierById_existingSupplier_returnsSupplier() throws Exception {
        SupplierResponse response =
                new SupplierResponse("1", "Bianca Popescu", "Fresh Farm Supply", "bianca@freshfarm.ro", "+40700111222");
        when(supplierService.getSupplierById("1")).thenReturn(response);

        mockMvc.perform(get("/api/suppliers/{id}", "1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName", is("Fresh Farm Supply")))
                .andExpect(jsonPath("$.email", is("bianca@freshfarm.ro")));

        verify(supplierService, times(1)).getSupplierById("1");
    }

    @Test
    void testGetAllSuppliers_returnsList() throws Exception {
        SupplierResponse response =
                new SupplierResponse("1", "Bianca Popescu", "Fresh Farm Supply", "bianca@freshfarm.ro", "+40700111222");
        when(supplierService.getAllSuppliers()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/suppliers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].companyName", is("Fresh Farm Supply")));
    }

    @Test
    void testCreateSupplier_validRequest_createsSupplier() throws Exception {
        CreateSupplierRequest request = new CreateSupplierRequest(
                "Bianca Popescu",
                "Fresh Farm Supply",
                "bianca@freshfarm.ro",
                "+40700111222"
        );
        SupplierResponse response =
                new SupplierResponse("1", "Bianca Popescu", "Fresh Farm Supply", "bianca@freshfarm.ro", "+40700111222");
        when(supplierService.createSupplier(any(CreateSupplierRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("1")))
                .andExpect(jsonPath("$.name", is("Bianca Popescu")));

        verify(supplierService, times(1)).createSupplier(any(CreateSupplierRequest.class));
    }

    @Test
    void testUpdateSupplier_existingSupplier_updatesSupplier() throws Exception {
        UpdateSupplierRequest request = new UpdateSupplierRequest(
                "Bianca Popescu",
                "Fresh Farm Supply SRL",
                "bianca@freshfarm.ro",
                "+40700999888"
        );
        SupplierResponse response =
                new SupplierResponse("1", "Bianca Popescu", "Fresh Farm Supply SRL", "bianca@freshfarm.ro",
                        "+40700999888");
        when(supplierService.updateSupplier(eq("1"), any(UpdateSupplierRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/suppliers/{id}", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName", is("Fresh Farm Supply SRL")))
                .andExpect(jsonPath("$.phoneNumber", is("+40700999888")));

        verify(supplierService, times(1)).updateSupplier(eq("1"), any(UpdateSupplierRequest.class));
    }

    @Test
    void testGetSupplierById_missingSupplier_returnsNotFound() throws Exception {
        when(supplierService.getSupplierById("missing")).thenThrow(new EntityNotFoundException("missing"));

        mockMvc.perform(get("/api/suppliers/{id}", "missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testDeleteSupplier_existingSupplier_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/suppliers/{id}", "1"))
                .andExpect(status().isNoContent());

        verify(supplierService, times(1)).deleteSupplier("1");
    }

    @Test
    void testDeleteSupplier_withAssignedInventory_returnsBadRequest() throws Exception {
        doThrow(new IllegalArgumentException("Supplier is still assigned to inventory items: 1"))
                .when(supplierService).deleteSupplier("1");

        mockMvc.perform(delete("/api/suppliers/{id}", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}
