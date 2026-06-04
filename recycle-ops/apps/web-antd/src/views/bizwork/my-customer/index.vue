<script lang="ts" setup>
import { Page } from '@vben/common-ui';

// import CustListTable from '../customer/cust-list-table.vue';
import CustListTableV2 from '../customer/cust-list-table-v2.vue';

defineOptions({ name: 'BizWorkMyCustomerPage' });
</script>
<template>
  <Page>
    <CustListTableV2 key="my-customers" :favorite-flag="1" />
  </Page>
</template>
