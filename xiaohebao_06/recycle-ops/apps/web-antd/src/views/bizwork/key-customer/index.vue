<script lang="ts" setup>
import { Page } from '@vben/common-ui';

// import CustListTable from '../customer/cust-list-table.vue';
import CustListTableV2 from '../customer/cust-list-table-v2.vue';

defineOptions({ name: 'BizWorkKeyCustomerPage' });
</script>
<template>
  <Page>
    <CustListTableV2 key="key-customers" :favorite-flag="2" />
  </Page>
</template>
