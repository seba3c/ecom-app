from fastapi import APIRouter, Depends, status

from app.api.dependencies import current_user, get_address_repository
from app.models.user import User
from app.repositories.addresses import AddressRepository
from app.schemas.address import AddressDetail, AddressInput, AddressList
from app.services.address_service import AddressService

router = APIRouter(prefix="/addresses", tags=["addresses"])


@router.get("", response_model=AddressList)
async def list_addresses(
    user: User = Depends(current_user),
    repository: AddressRepository = Depends(get_address_repository),
):
    return AddressList(content=await repository.list(user.id))


@router.get("/{address_id}", response_model=AddressDetail)
async def get_address(
    address_id: int,
    user: User = Depends(current_user),
    repository: AddressRepository = Depends(get_address_repository),
):
    return await AddressService(repository).address(user.id, address_id)


@router.post("", response_model=AddressDetail, status_code=status.HTTP_201_CREATED)
async def create_address(
    data: AddressInput,
    user: User = Depends(current_user),
    repository: AddressRepository = Depends(get_address_repository),
):
    return await AddressService(repository).create_address(user.id, data)


@router.put("/{address_id}", response_model=AddressDetail)
async def update_address(
    address_id: int,
    data: AddressInput,
    user: User = Depends(current_user),
    repository: AddressRepository = Depends(get_address_repository),
):
    return await AddressService(repository).update_address(user.id, address_id, data)


@router.delete("/{address_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_address(
    address_id: int,
    user: User = Depends(current_user),
    repository: AddressRepository = Depends(get_address_repository),
):
    await AddressService(repository).delete_address(user.id, address_id)
