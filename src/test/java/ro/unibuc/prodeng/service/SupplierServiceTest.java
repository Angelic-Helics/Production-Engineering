package ro.unibuc.prodeng.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ro.unibuc.prodeng.exception.EntityNotFoundException;
import ro.unibuc.prodeng.model.SupplierEntity;
import ro.unibuc.prodeng.repository.InventoryItemRepository;
import ro.unibuc.prodeng.repository.SupplierRepository;
import ro.unibuc.prodeng.request.CreateSupplierRequest;
import ro.unibuc.prodeng.request.UpdateSupplierRequest;
import ro.unibuc.prodeng.response.SupplierResponse;

@ExtendWith(SpringExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @InjectMocks
    private SupplierService supplierService;

    @Test
    void testGetAllSuppliers_returnsMappedResponses() {
        when(supplierRepository.findAll()).thenReturn(List.of(
                new SupplierEntity("1", "Bianca", "Fresh Farm Supply", "bianca@freshfarm.ro", "+40700111222"),
                new SupplierEntity("2", "Alex", "Market Greens", "alex@greens.ro", "+40700333444")
        ));

        List<SupplierResponse> result = supplierService.getAllSuppliers();

        assertEquals(2, result.size());
        assertEquals("Fresh Farm Supply", result.get(0).companyName());
        assertEquals("alex@greens.ro", result.get(1).email());
    }

    @Test
    void testCreateSupplier_duplicateEmail_throwsIllegalArgumentException() {
        CreateSupplierRequest request =
                new CreateSupplierRequest("Bianca", "Fresh Farm Supply", "bianca@freshfarm.ro", "+40700111222");
        when(supplierRepository.findByEmail("bianca@freshfarm.ro"))
                .thenReturn(Optional.of(new SupplierEntity("1", "Bianca", "Fresh Farm Supply",
                        "bianca@freshfarm.ro", "+40700111222")));

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> supplierService.createSupplier(request));

        assertTrue(exception.getMessage().contains("Supplier email already exists"));
    }

    @Test
    void testUpdateSupplier_validRequest_updatesSupplier() {
        SupplierEntity existing =
                new SupplierEntity("1", "Bianca", "Fresh Farm Supply", "bianca@freshfarm.ro", "+40700111222");
        when(supplierRepository.findById("1")).thenReturn(Optional.of(existing));
        when(supplierRepository.findByEmail(anyString())).thenReturn(Optional.of(existing));
        when(supplierRepository.save(any(SupplierEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SupplierResponse result = supplierService.updateSupplier(
                "1",
                new UpdateSupplierRequest("Bianca Popescu", "Fresh Farm Supply SRL",
                        "bianca@freshfarm.ro", "+40700999888")
        );

        assertEquals("Bianca Popescu", result.name());
        assertEquals("Fresh Farm Supply SRL", result.companyName());
        assertEquals("+40700999888", result.phoneNumber());
    }

    @Test
    void testDeleteSupplier_missingSupplier_throwsEntityNotFoundException() {
        when(supplierRepository.existsById("missing")).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> supplierService.deleteSupplier("missing"));
    }

    @Test
    void testDeleteSupplier_withAssignedInventory_throwsIllegalArgumentException() {
        when(supplierRepository.existsById("1")).thenReturn(true);
        when(inventoryItemRepository.findBySupplierId("1")).thenReturn(List.of(
                new ro.unibuc.prodeng.model.InventoryItemEntity("inv-1", "Mozzarella", "kg",
                        java.math.BigDecimal.ONE, java.math.BigDecimal.ONE, "1")
        ));

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> supplierService.deleteSupplier("1"));

        assertTrue(exception.getMessage().contains("still assigned"));
        verify(supplierRepository, times(0)).deleteById("1");
    }
}
