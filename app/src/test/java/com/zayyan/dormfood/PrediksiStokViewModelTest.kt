package com.zayyan.dormfood

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.zayyan.dormfood.ui.model.PrediksiMenuResponse
import com.zayyan.dormfood.ui.repository.IPrediksiRepository
import com.zayyan.dormfood.ui.viewmodel.PrediksiStokViewModel
import com.zayyan.dormfood.ui.viewmodel.PrediksiUiState
import com.zayyan.dormfood.ui.viewmodel.WarningLevel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrediksiStokViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var mockRepository: IPrediksiRepository
    private lateinit var viewModel: PrediksiStokViewModel

    private fun fakeResponse(
        status: String = "OK",
        totalMenu: Int = 5,
        menuSiapPrediksi: Int = 3,
        menuDataKurang: Int = 0
    ) = PrediksiMenuResponse(
        status = status,
        totalMenu = totalMenu,
        menuSiapPrediksi = menuSiapPrediksi,
        menuDataKurang = menuDataKurang
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk()
        viewModel = PrediksiStokViewModel(repository = mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `state awal adalah Idle`() {
        assertEquals(PrediksiUiState.Idle, viewModel.prediksiState.value)
    }

    @Test
    fun `data normal - warningLevel NONE`() = runTest {
        coEvery { mockRepository.getPrediksi(any()) } returns
                Result.success(fakeResponse(menuSiapPrediksi = 5, menuDataKurang = 0))

        viewModel.loadPrediksi("warung-1")

        val state = viewModel.prediksiState.value as PrediksiUiState.Success
        assertEquals(WarningLevel.NONE, state.warningLevel)
    }

    @Test
    fun `ada menu kurang data - warningLevel SEBAGIAN_KURANG`() = runTest {
        coEvery { mockRepository.getPrediksi(any()) } returns
                Result.success(fakeResponse(menuSiapPrediksi = 3, menuDataKurang = 2))

        viewModel.loadPrediksi("warung-1")

        val state = viewModel.prediksiState.value as PrediksiUiState.Success
        assertEquals(WarningLevel.SEBAGIAN_KURANG, state.warningLevel)
    }

    @Test
    fun `menuSiapPrediksi 0 - pakai dummy - warningLevel SEMUA_KURANG`() = runTest {
        val dummy = fakeResponse(status = "DUMMY", menuSiapPrediksi = 4)
        coEvery { mockRepository.getPrediksi(any()) } returns
                Result.success(fakeResponse(menuSiapPrediksi = 0))
        coEvery { mockRepository.getPrediksiDummy(any()) } returns Result.success(dummy)

        viewModel.loadPrediksi("warung-1")

        val state = viewModel.prediksiState.value as PrediksiUiState.Success
        assertEquals(WarningLevel.SEMUA_KURANG, state.warningLevel)
        assertEquals(dummy, state.data)
    }

    @Test
    fun `status NO_DATA - pakai dummy - warningLevel NO_DATA`() = runTest {
        val dummy = fakeResponse(status = "DUMMY")
        coEvery { mockRepository.getPrediksi(any()) } returns
                Result.success(fakeResponse(status = "NO_DATA"))
        coEvery { mockRepository.getPrediksiDummy(any()) } returns Result.success(dummy)

        viewModel.loadPrediksi("warung-1")

        val state = viewModel.prediksiState.value as PrediksiUiState.Success
        assertEquals(WarningLevel.NO_DATA, state.warningLevel)
        assertEquals(dummy, state.data)
    }

    @Test
    fun `dummy gagal - fallback ke response asli`() = runTest {
        val mainResponse = fakeResponse(menuSiapPrediksi = 0)
        coEvery { mockRepository.getPrediksi(any()) } returns Result.success(mainResponse)
        coEvery { mockRepository.getPrediksiDummy(any()) } returns
                Result.failure(Exception("dummy error"))

        viewModel.loadPrediksi("warung-1")

        val state = viewModel.prediksiState.value as PrediksiUiState.Success
        assertEquals(mainResponse, state.data)
    }

    @Test
    fun `network error - state menjadi Error`() = runTest {
        coEvery { mockRepository.getPrediksi(any()) } returns
                Result.failure(Exception("timeout"))

        viewModel.loadPrediksi("warung-1")

        val state = viewModel.prediksiState.value as PrediksiUiState.Error
        assertTrue(state.message.contains("timeout"))
    }

    @Test
    fun `loading state muncul sebelum result`() = runTest {
        val standardDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(standardDispatcher)

        coEvery { mockRepository.getPrediksi(any()) } coAnswers {
            kotlinx.coroutines.delay(1000)
            Result.success(fakeResponse())
        }

        val vmPaused = PrediksiStokViewModel(repository = mockRepository)

        vmPaused.loadPrediksi("warung-1")

        runCurrent()

        assertEquals(
            PrediksiUiState.Loading,
            vmPaused.prediksiState.value
        )

        advanceUntilIdle()

        assertTrue(
            vmPaused.prediksiState.value is PrediksiUiState.Success
        )
    }
}